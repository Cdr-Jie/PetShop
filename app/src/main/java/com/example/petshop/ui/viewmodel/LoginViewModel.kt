package com.example.petshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petshop.data.database.PetShopDatabase
import com.example.petshop.data.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val userDao = PetShopDatabase.getInstance(app).userDao()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please fill in all fields")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val user = userDao.login(username.trim(), password.trim())
            _uiState.value = if (user != null && user.isActive)
                LoginUiState.Success(user)
            else
                LoginUiState.Error("Invalid username or password")
        }
    }

    fun resetState() { _uiState.value = LoginUiState.Idle }
}

