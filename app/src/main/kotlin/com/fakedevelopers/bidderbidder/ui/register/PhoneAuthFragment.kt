package com.fakedevelopers.bidderbidder.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.MainActivity
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PhoneAuthFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentPhoneAuthBinding

    private val binding get() = _binding
    private val viewModel: PhoneAuthViewModel by viewModels()

    private val callbacks by lazy {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // 인증이 끝난 상태
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Logger.t("Auth").i("onVerificationCompleted")
                viewModel.setCodeSendingStates(PhoneAuthState.SENT)
            }
            // 인증 실패 상태
            override fun onVerificationFailed(e: FirebaseException) {
                Logger.t("Auth").i("onVerificationFailed")
                viewModel.setCodeSendingStates(PhoneAuthState.INIT)
            }
            // 전화번호는 확인 했고 인증코드를 입력해야 하는 상태
            override fun onCodeSent(verificationCode: String, resendingToken: PhoneAuthProvider.ForceResendingToken) {
                Logger.t("Auth").i("onCodeSent")
                // 인증 id 저장
                viewModel.setVerificationCodeAndResendingToken(verificationCode, resendingToken)
                super.onCodeSent(verificationCode, resendingToken)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainActivity = activity as MainActivity
        _binding = DataBindingUtil.inflate<FragmentPhoneAuthBinding>(
            inflater,
            R.layout.fragment_phone_auth,
            container,
            false
        ).apply {
            vm = viewModel
            lifecycleOwner = this@PhoneAuthFragment
        }
        initCollector()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 인증 번호 발송 버튼
        binding.buttonPhoneauthNextstep.setOnClickListener {
            when (viewModel.codeSendingStates.value) {
                PhoneAuthState.INIT -> sendPhoneAuthCode()
                PhoneAuthState.SENT -> signInWithPhoneAuthCredential()
                else -> {}
            }
        }
        binding.textviewPhoneauthResend.setOnClickListener {
            sendPhoneAuthCode()
        }
    }

    private fun initCollector() {
        // 코드 발송 상태에 따라 버튼 메세지가 바뀜
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.codeSendingStates.collectLatest { state -> handlePhoneAuthEvent(state) }
            }
        }
    }

    private fun handlePhoneAuthEvent(state: PhoneAuthState) {
        with(binding) {
            when (state) {
                PhoneAuthState.INIT -> {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_getauthcode)
                    textinputlayoutPhoneauthAuthcode.visibility = View.INVISIBLE
                    textviewPhoneauthResend.visibility = View.INVISIBLE
                }
                PhoneAuthState.SENDING -> {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_sending_authcode)
                    textinputlayoutPhoneauthAuthcode.visibility = View.INVISIBLE
                    textviewPhoneauthResend.visibility = View.INVISIBLE
                }
                PhoneAuthState.SENT -> {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_nextstep)
                    textinputlayoutPhoneauthAuthcode.visibility = View.VISIBLE
                    textviewPhoneauthResend.visibility = View.VISIBLE
                }
            }
            edittextPhoneauthAuthcode.isEnabled = state == PhoneAuthState.SENT
            // 보내는 중에는 버튼을 막아놓는다
            buttonPhoneauthNextstep.isEnabled = state != PhoneAuthState.SENDING
        }
    }

    // 인증 번호 보내기
    private fun sendPhoneAuthCode() {
        val options = PhoneAuthOptions.newBuilder(viewModel.auth)
            .setPhoneNumber("+82${viewModel.phoneNumber.value}")
            .setTimeout(EXPIRE_TIME, TimeUnit.SECONDS)
            .setActivity(mainActivity)
            .setCallbacks(callbacks)
        viewModel.requestSendPhoneAuth(options)
    }

    // 인증 번호 검사
    private fun signInWithPhoneAuthCredential() {
        // 검사 받는 동안은 버튼을 막아 둔다.
        binding.buttonPhoneauthNextstep.isEnabled = false
        PhoneAuthProvider.getCredential(viewModel.verificationId.value, viewModel.authCode.value).let {
            viewModel.auth.signInWithCredential(it)
                .addOnCompleteListener(mainActivity) { task ->
                    if (task.isSuccessful) {
                        Logger.t("Auth").i("인증 성공")
                        // 토큰 받아서 넘겨주기
                        task.result.user!!.getIdToken(true).addOnSuccessListener { result ->
                            Logger.t("Auth").i(result.token!!)
                            toRegisterFragment(result.token!!)
                        }
                    } else {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Logger.t("Auth").i("코드가 맞지 않음")
                        }
                        binding.buttonPhoneauthNextstep.isEnabled = true
                    }
                }
        }
    }

    private fun toRegisterFragment(token: String) {
        PhoneAuthFragmentDirections.actionPhoneAuthFragmentToRegisterFragment(token).let {
            findNavController().navigate(it)
        }
    }

    companion object {
        private const val EXPIRE_TIME = 120L
    }
}
