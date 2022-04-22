package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val _auth: FirebaseAuth
) : ViewModel() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    val phoneNumber = MutableStateFlow("")
    val authCode = MutableStateFlow("")
    private val _verificationId = MutableStateFlow("")
    private val _isCodeSending = MutableStateFlow(false)

    val auth get() = _auth
    val verificationId: StateFlow<String> get() = _verificationId
    val isCodeSending: StateFlow<Boolean> get() = _isCodeSending

    fun setCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
        _verificationId.value = id
        resendToken = token
    }

    fun requestSendPhoneAuth(options: PhoneAuthOptions.Builder) {
        // 재전송 토큰이 있다면 재전송
        if (::resendToken.isInitialized) {
            options.setForceResendingToken(resendToken)
        } else {
            viewModelScope.launch {
                _isCodeSending.emit(true)
            }
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }
}
