package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Service
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServiceViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)

    val services: StateFlow<List<Service>> =
        db.serviceDao().getAllServices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<String>> =
        db.serviceDao().getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addService(
        name: String, category: String,
        description: String, price: Double, durationMinutes: Int
    ) = viewModelScope.launch {
        db.serviceDao().insert(
            Service(
                name            = name.trim(),
                category        = category.trim(),
                description     = description.trim(),
                price           = price,
                durationMinutes = durationMinutes
            )
        )
    }

    fun toggleActive(service: Service) = viewModelScope.launch {
        db.serviceDao().setActiveStatus(service.serviceId, !service.isActive)
    }

    fun deleteService(service: Service) = viewModelScope.launch {
        db.serviceDao().delete(service)
    }
}

