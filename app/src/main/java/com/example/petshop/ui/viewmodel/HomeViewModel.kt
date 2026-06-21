package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Appointment
import com.example.petshop.data.entity.Client
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.User
import com.example.petshop.data.entity.UserRole
import com.example.petshop.data.relation.AppointmentWithDetails
import com.example.petshop.ui.navigation.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class StaffHomeSummary(
    val appointmentsToday: Int = 0,
    val upcomingAppointments: Int = 0,
    val totalClients: Int = 0,
    val totalPets: Int = 0,
    val lowStockItems: Int = 0,
)

data class ClientHomeSummary(
    val myPetsCount: Int = 0,
    val myAppointmentsToday: Int = 0,
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)
    private val currentUser = SessionManager.currentUser

    private val _myClient = MutableStateFlow<Client?>(null)
    val myClient: StateFlow<Client?> = _myClient.asStateFlow()

    private val _myUser = MutableStateFlow<User?>(currentUser)
    val myUser: StateFlow<User?> = _myUser.asStateFlow()

    private val _currentStaffId = MutableStateFlow<Int?>(null)

    private val _myPets = MutableStateFlow<List<Pet>>(emptyList())
    val myPets: StateFlow<List<Pet>> = _myPets.asStateFlow()

    private val _myTodayAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val myTodayAppointments: StateFlow<List<Appointment>> = _myTodayAppointments.asStateFlow()

    private val _clientSummary = MutableStateFlow(ClientHomeSummary())
    val clientSummary: StateFlow<ClientHomeSummary> = _clientSummary.asStateFlow()

    val staffSummary: StateFlow<StaffHomeSummary>
    val todaySchedule: StateFlow<List<AppointmentWithDetails>>

    init {
        val (startOfDay, endOfDay) = todayRange()

        staffSummary = combine(
            db.appointmentDao().getAppointmentsForDay(startOfDay, endOfDay),
            db.appointmentDao().getUpcomingAppointments(),
            db.clientDao().getAllClients(),
            db.petDao().getAllPets(),
            db.medicineInventoryDao().getLowStockItems()
        ) { todayApts, upcomingApts, clients, pets, lowStock ->
            StaffHomeSummary(
                appointmentsToday = todayApts.size,
                upcomingAppointments = upcomingApts.size,
                totalClients = clients.size,
                totalPets = pets.size,
                lowStockItems = lowStock.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StaffHomeSummary())

        todaySchedule = combine(
            db.appointmentDao().getAllWithDetails(),
            _myClient,
            _currentStaffId
        ) { allAppointments, linkedClient, currentStaffId ->
            allAppointments
                .filter { item ->
                    item.appointment.scheduledAt in startOfDay..endOfDay && when (currentUser?.role) {
                        UserRole.ADMIN -> true
                        UserRole.STAFF -> item.appointment.staffId == currentStaffId
                        UserRole.CLIENT -> linkedClient != null && item.appointment.clientId == linkedClient.clientId
                        else -> false
                    }
                }
                .sortedBy { it.appointment.scheduledAt }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        if (currentUser != null) {
            viewModelScope.launch {
                val freshUser = db.userDao().getById(currentUser.userId)
                if (freshUser != null) {
                    _myUser.value = freshUser
                    SessionManager.currentUser = freshUser
                }

                val linkedStaff = db.staffDao().getByUserId(currentUser.userId)
                _currentStaffId.value = linkedStaff?.staffId

                val linkedClient = db.clientDao().getByUserId(currentUser.userId)
                _myClient.value = linkedClient
                if (linkedClient != null) {
                    launch {
                        db.petDao().getPetsByClient(linkedClient.clientId).collect { pets ->
                            _myPets.value = pets
                            _clientSummary.update { it.copy(myPetsCount = pets.size) }
                        }
                    }
                    launch {
                        db.appointmentDao().getAppointmentsByClient(linkedClient.clientId).collect { appointments ->
                            val today = appointments.filter { it.scheduledAt in startOfDay..endOfDay }
                            _myTodayAppointments.value = today.sortedBy { it.scheduledAt }
                            _clientSummary.update { it.copy(myAppointmentsToday = today.size) }
                        }
                    }
                }
            }
        }
    }

    fun updateMyProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        avatarImageUri: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        val activeUser = _myUser.value ?: SessionManager.currentUser
        if (activeUser == null) {
            onResult(false, "No active user session.")
            return@launch
        }

        runCatching {
            val updatedUser = activeUser.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim(),
                phone = phone.trim(),
                avatarImageUri = avatarImageUri.trim()
            )
            db.userDao().update(updatedUser)

            val linkedClient = _myClient.value
            if (linkedClient != null) {
                db.clientDao().update(
                    linkedClient.copy(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        email = email.trim(),
                        phone = phone.trim()
                    )
                )
            }

            _myUser.value = updatedUser
            SessionManager.currentUser = updatedUser
            onResult(true, null)
        }.onFailure {
            onResult(false, it.message ?: "Unable to update profile.")
        }
    }

    fun updatePet(
        pet: Pet,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) = viewModelScope.launch {
        runCatching {
            db.petDao().update(pet)
            onResult(true, null)
        }.onFailure {
            onResult(false, it.message ?: "Unable to update pet.")
        }
    }

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return start to cal.timeInMillis
    }
}
