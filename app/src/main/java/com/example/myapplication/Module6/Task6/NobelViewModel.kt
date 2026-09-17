package com.example.myapplication.Module6.Task6

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NobelUiState {
    data object Loading : NobelUiState
    data class Success(val prizes: List<NobelPrize>) : NobelUiState
    data class Error(val message: String) : NobelUiState
}

class NobelViewModel(
    private val repository: NobelRepository = NobelRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<NobelUiState>(NobelUiState.Loading)
    val uiState: StateFlow<NobelUiState> = _uiState.asStateFlow()

    private var currentYear = "2010"
    private var currentCategory = "Physics"

    init {
        fetchPrizes(currentYear, currentCategory)
    }

    fun fetchPrizes(year: String = currentYear, category: String = currentCategory) {
        currentYear = year
        currentCategory = category
        _uiState.value = NobelUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.getPrizes(year, category)
                _uiState.value = NobelUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = NobelUiState.Error(
                    e.localizedMessage ?: "Не удалось связаться с локальным Ktor-сервером"
                )
            }
        }
    }
}