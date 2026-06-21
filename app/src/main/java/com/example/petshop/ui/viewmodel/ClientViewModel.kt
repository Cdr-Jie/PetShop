package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.Client
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ClientViewModel(app: Application) : AndroidViewModel(app) {

    private val db = PetShopDatabase.getInstance(app)

    val searchQuery = MutableStateFlow("")

    val clients: StateFlow<List<Client>> = searchQuery
        .debounce(300)
        .flatMapLatest { q ->
            if (q.isBlank()) db.clientDao().getAllClients()
            else db.clientDao().searchClients(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addClient(firstName: String, lastName: String, phone: String, email: String, address: String) =
        viewModelScope.launch {
            db.clientDao().insert(
                Client(
                    firstName = firstName.trim(),
                    lastName  = lastName.trim(),
                    phone     = phone.trim(),
                    email     = email.trim(),
                    address   = address.trim()
                )
            )
        }

    fun deleteClient(client: Client) = viewModelScope.launch {
        db.clientDao().delete(client)
    }
}

