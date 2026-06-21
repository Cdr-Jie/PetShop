package com.example.petshop.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.*
import com.example.petshop.data.relation.AppointmentWithDetails
import com.example.petshop.ui.navigation.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)
    private val currentUser = SessionManager.currentUser
    private val isClientView = currentUser?.role == UserRole.CLIENT

    private val userClientId: StateFlow<Int?> = flow {
        if (isClientView && currentUser != null) {
            val linkedClient = db.clientDao().getByUserId(currentUser.userId)
            emit(linkedClient?.clientId)
        } else {
            emit(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val appointments: StateFlow<List<AppointmentWithDetails>> =
        if (isClientView) {
            userClientId
                .flatMapLatest { clientId ->
                    if (clientId == null) flowOf(emptyList())
                    else db.appointmentDao().getWithDetailsByClient(clientId)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        } else {
            db.appointmentDao().getAllWithDetails()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        }

    val clients: StateFlow<List<Client>> =
        (if (isClientView) flowOf(emptyList()) else db.clientDao().getAllClients())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val staffList: StateFlow<List<Staff>> =
        (if (isClientView) flowOf(emptyList()) else db.staffDao().getActiveStaff())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val services: StateFlow<List<Service>> =
        (if (isClientView) flowOf(emptyList()) else db.serviceDao().getActiveServices())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedClientId = MutableStateFlow(-1)

    val petsForClient: StateFlow<List<Pet>> = _selectedClientId
        .filter { it > 0 }
        .flatMapLatest { db.petDao().getPetsByClient(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _addError = MutableStateFlow<String?>(null)
    val addError: StateFlow<String?> = _addError.asStateFlow()

    fun selectClient(clientId: Int) { _selectedClientId.value = clientId }

    fun clearAddError() { _addError.value = null }

    fun addAppointment(
        clientId: Int,
        petId: Int,
        staffId: Int?,
        serviceId: Int?,
        scheduledAt: Long,
        notes: String,
        onResult: (Boolean) -> Unit = {}
    ) = viewModelScope.launch {
        val petConflicts = db.appointmentDao().countPetConflicts(
            petId = petId,
            scheduledAt = scheduledAt
        )

        if (petConflicts > 0) {
            _addError.value = "Timeslot is already booked. Please choose another schedule."
            onResult(false)
            return@launch
        }

        try {
            var bookingFailed = false
            var failureMessage: String? = null

            db.withTransaction {
                val slotId = if (staffId != null) {
                    val serviceDurationMinutes = serviceId
                        ?.let { db.serviceDao().getById(it)?.durationMinutes }
                        ?: 30
                    val slotEndTime = scheduledAt + serviceDurationMinutes * 60_000L

                    val existingSlot = db.timeSlotDao().findByStartTime(staffId, scheduledAt)
                    val resolvedSlotId = existingSlot?.timeSlotId
                        ?: db.timeSlotDao().insert(
                            TimeSlot(
                                staffId = staffId,
                                slotStartTime = scheduledAt,
                                slotEndTime = slotEndTime
                            )
                        ).toInt()

                    val activeAppointments = db.appointmentDao().countActiveByTimeSlot(resolvedSlotId)
                    if (activeAppointments > 0 || existingSlot?.isBooked == true) {
                        bookingFailed = true
                        failureMessage = "Selected staff timeslot is already booked."
                        null
                    } else {
                        resolvedSlotId
                    }
                } else {
                    null
                }

                if (!bookingFailed) {
                    db.appointmentDao().insert(
                        Appointment(
                            clientId = clientId,
                            petId = petId,
                            staffId = staffId,
                            serviceId = serviceId,
                            timeSlotId = slotId,
                            scheduledAt = scheduledAt,
                            notes = notes
                        )
                    )

                    if (slotId != null) {
                        db.timeSlotDao().updateBookedState(slotId, true)
                    }
                }
            }

            if (bookingFailed) {
                _addError.value = failureMessage ?: "Timeslot is already booked. Please choose another schedule."
                onResult(false)
                return@launch
            }

            _addError.value = null
            onResult(true)
        } catch (_: SQLiteConstraintException) {
            _addError.value = "Timeslot is already booked. Please choose another schedule."
            onResult(false)
        }
    }

    fun updateStatus(id: Int, status: AppointmentStatus) = viewModelScope.launch {
        db.withTransaction {
            db.appointmentDao().updateStatus(id, status)

            if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW) {
                val appointment = db.appointmentDao().getById(id)
                val slotId = appointment?.timeSlotId
                if (slotId != null) {
                    val activeAppointments = db.appointmentDao().countActiveByTimeSlot(slotId)
                    if (activeAppointments == 0) {
                        db.timeSlotDao().updateBookedState(slotId, false)
                    }
                }
            }
        }
    }
}
