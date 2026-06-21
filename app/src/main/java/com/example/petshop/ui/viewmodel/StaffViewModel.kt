package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Staff
import com.example.petshop.data.entity.StaffRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StaffViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)

    val staffList: StateFlow<List<Staff>> =
        db.staffDao().getAllStaff()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addStaff(
        firstName: String, lastName: String,
        phone: String, email: String,
        role: StaffRole, specialization: String
    ) = viewModelScope.launch {
        db.staffDao().insert(
            Staff(
                firstName      = firstName.trim(),
                lastName       = lastName.trim(),
                phone          = phone.trim(),
                email          = email.trim(),
                role           = role,
                specialization = specialization.trim()
            )
        )
    }

    fun toggleActive(staff: Staff) = viewModelScope.launch {
        db.staffDao().setActiveStatus(staff.staffId, !staff.isActive)
    }

    fun deleteStaff(staff: Staff) = viewModelScope.launch {
        db.staffDao().delete(staff)
    }
}

