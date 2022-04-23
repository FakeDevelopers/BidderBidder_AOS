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

    val phoneNumber = MutableStateFlow("")
    val authCode = MutableStateFlow("")
    private val resendingToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    private val _verificationId = MutableStateFlow("")
    private val _codeSendingStates = MutableStateFlow(PhoneAuthState.INIT)

    val auth get() = _auth
    val verificationId: StateFlow<String> get() = _verificationId
    val codeSendingStates: StateFlow<PhoneAuthState> get() = _codeSendingStates

    fun setVerificationCodeAndResendingToken(id: String, token: PhoneAuthProvider.ForceResendingToken) {
        _verificationId.value = id
        resendingToken.value = token
    }

    fun requestSendPhoneAuth(options: PhoneAuthOptions.Builder) {
        // 재전송 토큰이 있다면 재전송
        if (resendingToken.value != null) {
            options.setForceResendingToken(resendingToken.value!!)
        } else {
            setCodeSendingStates(PhoneAuthState.SENDING)
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    fun setCodeSendingStates(state: PhoneAuthState) {
        viewModelScope.launch {
            _codeSendingStates.emit(state)
        }
        if (state == PhoneAuthState.INIT) {
            resendingToken.value = null
            authCode.value = ""
        }
    }
}
