package com.fakedevelopers.bidderbidder.ui.register.phone_auth

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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val resendingToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    private val _codeSendingStates = MutableStateFlow(PhoneAuthState.INIT)
    private var verificationId = ""

    val phoneNumber = MutableStateFlow("")
    val authCode = MutableStateFlow("")
    val codeSendingStates: StateFlow<PhoneAuthState> get() = _codeSendingStates

    init {
        auth.useAppLanguage()
    }

    fun getAuthResult() = auth.signInWithCredential(PhoneAuthProvider.getCredential(verificationId, authCode.value))

    fun getAuthBuilder() = PhoneAuthOptions.newBuilder(auth)

    fun setVerificationCodeAndResendingToken(id: String, token: PhoneAuthProvider.ForceResendingToken) {
        verificationId = id
        resendingToken.value = token
    }

    fun requestSendPhoneAuth(options: PhoneAuthOptions.Builder) {
        // 재전송 토큰이 있다면 재전송
        resendingToken.value?.let {
            options.setForceResendingToken(it)
        } ?: setCodeSendingStates(PhoneAuthState.SENDING)
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
