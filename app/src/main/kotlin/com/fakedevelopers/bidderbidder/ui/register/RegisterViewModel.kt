package com.fakedevelopers.bidderbidder.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.fakedevelopers.bidderbidder.FragmentType
import com.fakedevelopers.bidderbidder.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.getCredential
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var auth: FirebaseAuth
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private val currentUser = MutableLiveData<FirebaseUser>()
    val phoneNumber = MutableLiveData<String>()
    val authCode = MutableLiveData<String>()
    val isCodeSending = MutableLiveData<Boolean>()
    val verificationId = MutableLiveData<String>()

    init {
        isCodeSending.value = false
        // 콜백 초기화
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // 인증이 끝난 상태
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Logger.t("Auth").i("onVerificationCompleted")
            }
            // 인증 실패 상태
            override fun onVerificationFailed(p0: FirebaseException) {
                Logger.t("Auth").i("onVerificationFailed")
            }
            // 전화번호는 확인 했고 인증코드를 입력해야 하는 상태
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                Logger.t("Auth").i("onCodeSent")
                // 인증 id 저장
                verificationId.value = p0
                super.onCodeSent(p0, p1)
            }
        }
    }

    // 인증 번호 보내기
    fun sendPhoneAuthCode(mainActivity: MainActivity) {
        auth = FirebaseAuth.getInstance().apply {
            setLanguageCode("ko")
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+82${phoneNumber.value}")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(mainActivity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        isCodeSending.value = true
    }

    // 인증 번호 검사
    fun signInWithPhoneAuthCredential(mainActivity: MainActivity) {
        getCredential(verificationId.value!!, authCode.value!!).let {
            auth.signInWithCredential(it)
                .addOnCompleteListener(mainActivity) { task ->
                    if (task.isSuccessful) {
                        Logger.t("Auth").i("인증 성공")
                        // 아직 어디 사용할지 모르겟슴
                        currentUser.value = task.result?.user
                        mainActivity.setFragment(FragmentType.REGISTER)
                    }
                    else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Logger.t("Auth").i("코드가 맞지 않음")
                        }
                    }
                }
        }
    }
}
