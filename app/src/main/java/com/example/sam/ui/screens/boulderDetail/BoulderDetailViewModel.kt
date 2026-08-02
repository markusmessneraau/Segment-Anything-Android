package com.example.sam.ui.screens.boulderDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sam.network.RetrofitClient
import com.example.sam.network.dto.BoulderDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoulderDetailViewModel : ViewModel() {

    private val _boulder = MutableStateFlow<BoulderDetailDto?>(null)
    val boulder: StateFlow<BoulderDetailDto?> = _boulder.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchBoulder(id: String) {
        viewModelScope.launch {
            _boulder.value = null // alten status löschen
            _isLoading.value = true
            _error.value = null
            try {
                val detail = RetrofitClient.apiService.getBoulderById(id)
                _boulder.value = detail
            } catch (e: Exception) {
                _error.value = "Fehler beim Laden des Boulders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
