package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.*
import com.example.petshop.data.relation.AppointmentWithDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)

    val appointments: StateFlow<List<AppointmentWithDetails>> =
        db.appointmentDao().getAllWithDetails()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val clients: StateFlow<List<Client>> =
        db.clientDao().getAllClients()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val staffList: StateFlow<List<Staff>> =
        db.staffDao().getActiveStaff()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val services: StateFlow<List<Service>> =
        db.serviceDao().getActiveServices()
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
        val conflicts = db.appointmentDao().countConflicts(
            scheduledAt = scheduledAt,
            petId = petId,
            staffId = staffId
        )

        if (conflicts > 0) {
            _addError.value = "Timeslot is already booked. Please choose another schedule."
            onResult(false)
            return@launch
        }

        db.appointmentDao().insert(
            Appointment(
                clientId = clientId,
                petId = petId,
                staffId = staffId,
                serviceId = serviceId,
                scheduledAt = scheduledAt,
                notes = notes
            )
        )
        _addError.value = null
        onResult(true)
    }

    fun updateStatus(id: Int, status: AppointmentStatus) = viewModelScope.launch {
        db.appointmentDao().updateStatus(id, status)
    }
}
