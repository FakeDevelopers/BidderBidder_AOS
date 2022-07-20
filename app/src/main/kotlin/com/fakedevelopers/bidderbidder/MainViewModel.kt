package com.fakedevelopers.bidderbidder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshed = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> get() = _isLoading
    val isRefreshed: StateFlow<Boolean> get() = _isRefreshed

    fun setReload(state: Boolean) {
        viewModelScope.launch {
            _isLoading.emit(state)
        }
        if (state) {
            setRefresh(true)
        }
    }
    fun setRefresh(state: Boolean) {
        viewModelScope.launch {
            _isRefreshed.emit(state)
        }
    }
}
