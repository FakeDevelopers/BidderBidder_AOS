package com.fakedevelopers.bidderbidder.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.bidderbidder.FragmentType
import com.fakedevelopers.bidderbidder.MainActivity
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentPhoneAuthBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

class PhoneAuthFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var binding: FragmentPhoneAuthBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        mainActivity = activity as MainActivity
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_phone_auth, container, false)
        registerViewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        // 뷰 모델과 데이터 바인딩 합체
        binding.vm = registerViewModel
        initCallbacks()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 인증 번호 발송 버튼
        binding.buttonPhoneauthNextstep.setOnClickListener {
            // 이미 코드를 보낸 상태인지 아닌지
            if(registerViewModel.isCodeSending.value!!){
                Logger.t("Auth").i(registerViewModel.authCode.value!!)
                // PhoneAuthCredential 객체 생성
                val credential = PhoneAuthProvider.getCredential(registerViewModel.verificationId.value!!, registerViewModel.authCode.value!!)
                // 인증 번호 확인
                signInWithPhoneAuthCredential(credential)
            } else {
                // 인증 번호 전송
                sendPhoneAuthCode(registerViewModel.phoneNumber.value!!)
            }
        }
    }

    // 콜백 초기화
    private fun initCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // 인증이 끝난 상태
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Logger.t("Auth").i("onVerificationCompleted")
            }
            // 인증 실패 상태
            override fun onVerificationFailed(p0: FirebaseException) {
                Logger.t("Auth").i("onVerificationFailed")
                Toast.makeText(context, "실패했어용", Toast.LENGTH_SHORT).show()
            }
            // 전화번호는 확인 했고 인증코드를 입력해야 하는 상태
            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                Logger.t("Auth").i("onCodeSent")
                Toast.makeText(context, "인증번호가 도착했어용", Toast.LENGTH_SHORT).show()
                // 인증 id 저장
                registerViewModel.verificationId.value = p0
                super.onCodeSent(p0, p1)
            }
        }
        // 코드 발송 상태에 따라 버튼 메세지가 바뀜
        registerViewModel.isCodeSending.observe(viewLifecycleOwner) {
            if(it) {
                binding.buttonPhoneauthNextstep.setText(R.string.phoneauth_nextstep)
                binding.textinputlayoutPhoneauthAuthcode.visibility = View.VISIBLE
                binding.edittextPhoneauthAuthcode.isEnabled = true
            }
            else {
                binding.buttonPhoneauthNextstep.setText(R.string.phoneauth_getauthcode)
                binding.textinputlayoutPhoneauthAuthcode.visibility = View.INVISIBLE
                binding.edittextPhoneauthAuthcode.isEnabled = false
            }
        }
    }

    // 인증 코드 전송
    private fun sendPhoneAuthCode(phoneNumber: String) {
        // 인증 번호 보내기
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("ko")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+82$phoneNumber")
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(mainActivity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        registerViewModel.isCodeSending.value = true
    }

    // 인증 코드 확인
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(mainActivity) { task ->
                if (task.isSuccessful) {
                    Logger.t("Auth").i("인증 성공")
                    registerViewModel.currentUser.value = task.result?.user
                    mainActivity.setFragment(FragmentType.REGISTER)
                }
                else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Logger.t("Auth").i("코드가 맞지 않음")
                    }
                }
            }
    }
    // SafetyNet 사용가능 여부
    // SafetyNet이 없으면 휴대폰 인증을 받기전에 리캡챠가 뜹니다.
    // 아직은 사용하지 않읍니다
    private fun isSafetyNetAvailable(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS
    }
}
