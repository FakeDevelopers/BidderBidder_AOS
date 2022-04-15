package com.fakedevelopers.bidderbidder.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.MainActivity
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentPhoneAuthBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.orhanobut.logger.Logger
import java.util.concurrent.TimeUnit

class PhoneAuthFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentPhoneAuthBinding
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding
    private val phoneAuthViewModel: PhoneAuthViewModel by lazy {
        ViewModelProvider(this)[PhoneAuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        mainActivity = activity as MainActivity
        _binding = DataBindingUtil.inflate<FragmentPhoneAuthBinding>(inflater, R.layout.fragment_phone_auth, container, false).also {
            // 뷰 모델과 데이터 바인딩 합체
            it.vm = phoneAuthViewModel
            it.lifecycleOwner = this
        }
        initCallbacks()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance().apply {
            setLanguageCode("ko")
        }
        // 인증 번호 발송 버튼
        binding.buttonPhoneauthNextstep.setOnClickListener {
            with(phoneAuthViewModel){
                if(isCodeSending.value!!){
                    // 인증 번호 확인
                    signInWithPhoneAuthCredential()
                } else {
                    // 인증 번호 전송
                    sendPhoneAuthCode()
                }
            }
        }
    }

    private fun initCallbacks() {
        // 코드 발송 상태에 따라 버튼 메세지가 바뀜
        phoneAuthViewModel.isCodeSending.observe(viewLifecycleOwner) {
            with(binding){
                if(it) {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_nextstep)
                    textinputlayoutPhoneauthAuthcode.visibility = View.VISIBLE
                }
                else {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_getauthcode)
                    textinputlayoutPhoneauthAuthcode.visibility = View.INVISIBLE
                }
                edittextPhoneauthAuthcode.isEnabled = it
            }
        }
        // 콜백 저장
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
                phoneAuthViewModel.verificationId.value = p0
                super.onCodeSent(p0, p1)
            }
        }
    }

    // 인증 번호 보내기
    private fun sendPhoneAuthCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+82${phoneAuthViewModel.phoneNumber.value}")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(mainActivity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        phoneAuthViewModel.isCodeSending.value = true
    }

    // 인증 번호 검사
    private fun signInWithPhoneAuthCredential() {
        PhoneAuthProvider.getCredential(phoneAuthViewModel.verificationId.value!!, phoneAuthViewModel.authCode.value!!).let {
            auth.signInWithCredential(it)
                .addOnCompleteListener(mainActivity) { task ->
                    if (task.isSuccessful) {
                        Logger.t("Auth").i("인증 성공")
                        // 토큰 받아서 넘겨주기
                        task.result.user!!.getIdToken(true).addOnSuccessListener { result ->
                            Logger.t("Auth").i(result.token!!)
                            val action = PhoneAuthFragmentDirections.actionPhoneAuthFragmentToRegisterFragment(result.token!!)
                            findNavController().navigate(action)
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

    // SafetyNet 사용가능 여부
    // 없으면 휴대폰 인증을 받기전에 리캡챠가 뜹니다.
    // 아직은 사용하지 않읍니다
    private fun isSafetyNetAvailable(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS
    }
}
