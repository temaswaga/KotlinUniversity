package com.example.myapplication.Module6.Task3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Login : AuthUiState
    data object Loading : AuthUiState
    data class UsersList(val users: List<User>) : AuthUiState
    data class UserDetail(val user: User) : AuthUiState
    data class Error(val message: String, val isLoginError: Boolean) : AuthUiState
}

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            if (repository.isAuthorized()) {
                loadUsers()
            } else {
                _uiState.value = AuthUiState.Login
            }
        }
    }

    fun login(user: String, pass: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repository.login(user, pass)
                loadUsers()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    message = e.localizedMessage ?: "Ошибка авторизации",
                    isLoginError = true
                )
            }
        }
    }

    fun loadUsers() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getUsers()
                _uiState.value = AuthUiState.UsersList(list)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    message = e.localizedMessage ?: "Ошибка загрузки пользователей",
                    isLoginError = false
                )
            }
        }
    }

    fun openUserDetails(user: User) {
        _uiState.value = AuthUiState.UserDetail(user)
    }

    fun backToUsersList() {
        loadUsers()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = AuthUiState.Login
        }
    }
}