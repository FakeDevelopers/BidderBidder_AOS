package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhoneAuthViewModel : ViewModel() {

    val phoneNumber = MutableStateFlow("")
    val authCode = MutableStateFlow("")
    private val _verificationId = MutableStateFlow("")
    private val _isCodeSending = MutableStateFlow(false)

    val verificationId: StateFlow<String> get() = _verificationId
    val isCodeSending: StateFlow<Boolean> get() = _isCodeSending

    fun setVerificationId(id: String) {
        _verificationId.value = id
    }

    fun setCodeSending(state: Boolean) {
        viewModelScope.launch {
            _isCodeSending.emit(state)
        }
    }
}
