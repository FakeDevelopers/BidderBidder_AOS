package com.fakedevelopers.bidderbidder.ui.register

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import com.fakedevelopers.bidderbidder.MainActivity
import com.fakedevelopers.bidderbidder.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.getCredential
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

class PhoneAuthViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var auth: FirebaseAuth
    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
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
    fun signInWithPhoneAuthCredential(mainActivity: MainActivity, view: View) {
        getCredential(verificationId.value!!, authCode.value!!).let {
            auth.signInWithCredential(it)
                .addOnCompleteListener(mainActivity) { task ->
                    if (task.isSuccessful) {
                        Logger.t("Auth").i("인증 성공")
                        // 토큰 받아서 넘겨주기
                        task.result.user!!.getIdToken(true).addOnSuccessListener { result ->
                            Logger.t("Auth").i(result.token!!)
                            val action = PhoneAuthFragmentDirections.actionPhoneAuthFragmentToRegisterFragment(result.token!!)
                            Navigation.findNavController(view).navigate(action)
                        }
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
