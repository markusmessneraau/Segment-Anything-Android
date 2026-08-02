package com.example.sam.ui.screens.boulderList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sam.network.RetrofitClient
import com.example.sam.network.dto.BoulderListDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoulderListViewModel : ViewModel() {

    private val _boulders = MutableStateFlow<List<BoulderListDto>>(emptyList())
    val boulders: StateFlow<List<BoulderListDto>> = _boulders.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        fetchBoulders()
    }

    fun fetchBoulders(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _isLoading.value = true
            }
            try {
                val fetchedBoulders = RetrofitClient.apiService.getAllBoulders()
                _boulders.value = fetchedBoulders
            } catch (e: Exception) {
                println("Fehler beim Laden der Boulder-Liste:")
                println(e.message)
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }
}


