package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Client
import com.example.petshop.data.entity.Pet
import com.example.petshop.data.entity.PetGender
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PetViewModel(app: Application, val clientId: Int) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)

    val pets: StateFlow<List<Pet>> =
        db.petDao().getPetsByClient(clientId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _client = MutableStateFlow<Client?>(null)
    val client: StateFlow<Client?> = _client.asStateFlow()

    init {
        viewModelScope.launch { _client.value = db.clientDao().getById(clientId) }
    }

    fun addPet(
        name: String, species: String, breed: String,
        gender: PetGender, birthDate: Long?, weightKg: Float?, profileImageUri: String
    ) = viewModelScope.launch {
        db.petDao().insert(
            Pet(
                clientId  = clientId,
                name      = name.trim(),
                species   = species.trim(),
                breed     = breed.trim(),
                gender    = gender,
                birthDate = birthDate,
                weightKg  = weightKg,
                profileImageUri = profileImageUri.trim()
            )
        )
    }

    fun updatePet(pet: Pet) = viewModelScope.launch {
        db.petDao().update(pet)
    }

    fun deletePet(pet: Pet) = viewModelScope.launch { db.petDao().delete(pet) }

    companion object {
        fun factory(clientId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                @Suppress("UNCHECKED_CAST")
                return PetViewModel(app, clientId) as T
            }
        }
    }
}

