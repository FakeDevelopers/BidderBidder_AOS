package com.fakedevelopers.presentation.ui.register.phoneAuth

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.presentation.ui.register.phoneAuth.PhoneAuthFragment.Companion.EXPIRE_TIME
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val timerFormat = DecimalFormat("00")
    private val _timerState = MutableEventFlow<Boolean>()
    private val _codeSendingStates = MutableEventFlow<PhoneAuthState>()
    private var verificationId = ""

    val phoneNumber = MutableStateFlow("")
    val authCode = MutableStateFlow("")
    val remainTime = MutableStateFlow("")
    val timerState = _timerState.asEventFlow()
    val codeSendingStates = _codeSendingStates.asEventFlow()
    var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
        private set

    private val timerTask by lazy {
        object : CountDownTimer(EXPIRE_TIME * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                setRemainTime(millisUntilFinished)
            }

            override fun onFinish() {
                // 타임오바 됐다면 초기 상태로 돌아간다.
                viewModelScope.launch {
                    _timerState.emit(false)
                }
                setCodeSendingStates(PhoneAuthState.INIT)
            }
        }
    }

    init {
        auth.useAppLanguage()
    }

    fun getAuthResult(): Task<AuthResult> {
        return auth.signInWithCredential(PhoneAuthProvider.getCredential(verificationId, authCode.value))
    }

    fun getAuthBuilder() = PhoneAuthOptions.newBuilder(auth)

    fun setVerificationCodeAndResendingToken(id: String, token: PhoneAuthProvider.ForceResendingToken) {
        verificationId = id
        resendingToken = token
    }

    fun setCodeSendingStates(state: PhoneAuthState) {
        viewModelScope.launch {
            _codeSendingStates.emit(state)
        }
        if (state == PhoneAuthState.INIT) {
            resendingToken = null
            authCode.value = ""
        }
    }

    fun startTimer() {
        timerTask.cancel()
        viewModelScope.launch {
            _timerState.emit(true)
        }
        timerTask.start()
    }

    private fun setRemainTime(millisUntilFinished: Long) {
        val minute = millisUntilFinished / 60000
        val second = millisUntilFinished % 60000 / 1000
        viewModelScope.launch {
            remainTime.emit("${timerFormat.format(minute)}:${timerFormat.format(second)}")
        }
    }

    fun isPhoneNumberCheck(): Boolean {
        val regex = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$".toRegex()

        return regex.matches(phoneNumber.value)
    }
}
