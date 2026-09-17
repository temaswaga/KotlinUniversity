package com.example.myapplication.Module6.Task1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface PhotosUiState {
    data object Loading : PhotosUiState
    data class Success(val photos: List<Photo>) : PhotosUiState
    data class Error(val message: String) : PhotosUiState
}

class PhotoViewModel(
    private val repository: PhotoRepository = PhotoRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotosUiState>(PhotosUiState.Loading)
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        _uiState.value = PhotosUiState.Loading
        viewModelScope.launch {
            try {
                val list = repository.fetchPhotos()
                _uiState.value = PhotosUiState.Success(list)
            } catch (e: HttpException) {
                _uiState.value = PhotosUiState.Error("HTTP ${e.code()}")
            } catch (e: IOException) {
                _uiState.value = PhotosUiState.Error("Ошибка сети. Проверьте интернет.")
            } catch (e: Exception) {
                _uiState.value = PhotosUiState.Error(e.localizedMessage ?: "Неизвестная ошибка")
            }
        }
    }
}