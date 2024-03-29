package com.fakedevelopers.presentation.ui.register.phoneAuth

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentPhoneAuthBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.register.RegistrationProgressState.CONGRATULATIONS
import com.fakedevelopers.presentation.ui.register.RegistrationProgressState.PHONE_AUTH_CHECK_AUTH_CODE
import com.fakedevelopers.presentation.ui.register.UserRegistrationViewModel
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PhoneAuthFragment : BaseFragment<FragmentPhoneAuthBinding>(
    R.layout.fragment_phone_auth
) {
    private val phoneAuthViewModel: PhoneAuthViewModel by viewModels()
    private val userRegistrationViewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    private val callbacks by lazy {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // 인증이 끝난 상태
            }

            // 인증 실패 상태
            // 보통 할당량이 다 떨어지면 여기로 간다
            override fun onVerificationFailed(e: FirebaseException) {
                sendSnackBar("저런 실패했군요!")
                phoneAuthViewModel.setCodeSendingStates(PhoneAuthState.INIT)
            }

            // 전화번호는 확인 했고 인증코드를 입력해야 하는 상태
            override fun onCodeSent(verificationCode: String, resendingToken: PhoneAuthProvider.ForceResendingToken) {
                phoneAuthViewModel.apply {
                    // 타이머 시작
                    startTimer()
                    // 인증 id 저장
                    setVerificationCodeAndResendingToken(verificationCode, resendingToken)
                    setCodeSendingStates(PhoneAuthState.SENT)
                }
                userRegistrationViewModel.setCurrentStep(PHONE_AUTH_CHECK_AUTH_CODE)
                super.onCodeSent(verificationCode, resendingToken)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = phoneAuthViewModel
    }

    override fun initListener() {
        binding.buttonPhoneauthSendCode.setOnClickListener {
            if (phoneAuthViewModel.phoneNumber.value.isNotEmpty()) {
                if (phoneAuthViewModel.isPhoneNumberCheck()) {
                    runFadeInAlertBox()
                    runFadeOutAlertBox()
                    sendPhoneAuthCode()
                    setPhoneValidInfo(R.string.phoneauth_number_is_valid, R.color.bidderbidder_primary, true)
                    setTextInputBackground(R.drawable.text_input_white_background_normal)
                } else {
                    setPhoneValidInfo(R.string.phoneauth_number_is_invalid, R.color.alert_red, false)
                    setTextInputBackground(R.drawable.text_input_white_background_error)
                }
            }
        }
    }

    override fun initCollector() {
        // 코드 발송 상태에 따라 버튼 메세지가 바뀜
        repeatOnStarted(viewLifecycleOwner) {
            phoneAuthViewModel.codeSendingStates.collectLatest { state ->
                handlePhoneAuthEvent(state)
            }
        }
        // 인증 코드 검사 요청
        repeatOnStarted(viewLifecycleOwner) {
            userRegistrationViewModel.checkAuthCode.collectLatest {
                signInWithPhoneAuthCredential()
            }
        }
        // 타이머 상태
        repeatOnStarted(viewLifecycleOwner) {
            phoneAuthViewModel.timerState.collectLatest {
                if (it) {
                    binding.textviewPhoneauthTimer.visibility = View.VISIBLE
                } else {
                    binding.textviewPhoneauthTimer.visibility = View.INVISIBLE
                    alertDialogWithButton(getString(R.string.phoneauth_session_expired_alert))
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            phoneAuthViewModel.phoneNumber.collectLatest {
                setPhoneValidInfo(R.string.phoneauth_number_is_valid, R.color.bidderbidder_primary, true)
                setTextInputBackground(R.drawable.text_input_white_background_normal)
                if (phoneAuthViewModel.isPhoneNumberCheck()) {
                    binding.buttonPhoneauthSendCode.setText(R.string.phoneauth_getauthcode)
                    setButtonTextColor(R.color.white)
                    setButtonBackground(R.drawable.button_phone_auth_before_send_ready)
                } else {
                    setButtonTextColor(R.color.black)
                    setButtonBackground(R.drawable.button_phone_auth_before_send)
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            // 인증 번호 6자리 되면 다음 버튼 활성화
            phoneAuthViewModel.authCode.collectLatest {
                userRegistrationViewModel.setAuthCode(it)
            }
        }
    }

    private fun runFadeInAlertBox() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.includeRegistrationAlert.root.apply {
            post {
                startAnimation(animation)
                visibility = View.VISIBLE
            }
        }
    }

    private fun runFadeOutAlertBox() {
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        animation.startOffset = 1000
        binding.includeRegistrationAlert.root.apply {
            post {
                startAnimation(animation)
                visibility = View.INVISIBLE
            }
        }
    }

    // 중복 체크 표시
    private fun setPhoneValidInfo(stringId: Int, colorId: Int, state: Boolean) {
        binding.textviewPhoneValidInfo.apply {
            visibility = if (state) View.INVISIBLE else View.VISIBLE
            setText(stringId)
            setTextColor(ContextCompat.getColor(requireContext(), colorId))
            this.isSelected = state
        }
    }

    private fun setTextInputBackground(drawableId: Int) {
        binding.edittextRegisterPhone.background =
            ContextCompat.getDrawable(requireContext(), drawableId)
    }

    private fun setButtonBackground(drawableId: Int) {
        binding.buttonPhoneauthSendCode.background =
            ContextCompat.getDrawable(requireContext(), drawableId)
    }

    private fun setButtonTextColor(color: Int) {
        binding.buttonPhoneauthSendCode.setTextColor(resources.getColor(color, null))
    }

    private fun handlePhoneAuthEvent(state: PhoneAuthState) {
        binding.buttonPhoneauthSendCode.apply {
            when (state) {
                PhoneAuthState.INIT -> setText(R.string.phoneauth_getauthcode)
                PhoneAuthState.SENDING -> setText(R.string.phoneauth_sending_authcode)
                PhoneAuthState.SENT -> setText(R.string.phoneauth_resend)
            }
            isEnabled = state != PhoneAuthState.SENDING
        }
        binding.edittextPhoneauthAuthcode.visibility = View.VISIBLE
        binding.edittextPhoneauthAuthcode.isEnabled = state == PhoneAuthState.SENT
    }

    private fun handleAuthResult(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            // 토큰 받아서 넘겨주기
            task.result.user?.let { user ->
                user.getIdToken(true).addOnSuccessListener { result ->
                    userRegistrationViewModel.apply {
                        setPhoneAuthToken(result.token ?: "")
                        setCurrentStep(CONGRATULATIONS)
                    }
                }
            }
        }
        userRegistrationViewModel.setNextStepEnabled(true)
    }

    // 인증 번호 보내기
    private fun sendPhoneAuthCode() {
        val options = phoneAuthViewModel.getAuthBuilder()
            .setPhoneNumber("+82${phoneAuthViewModel.phoneNumber.value}")
            .setTimeout(EXPIRE_TIME, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
        // 재전송 토큰이 있다면 재전송
        phoneAuthViewModel.resendingToken?.let {
            options.setForceResendingToken(it)
        } ?: phoneAuthViewModel.setCodeSendingStates(PhoneAuthState.SENDING)
        PhoneAuthProvider.verifyPhoneNumber(options.build())
    }

    // 인증 번호 검사
    private fun signInWithPhoneAuthCredential() {
        // 검사 받는 동안은 버튼을 막아 둔다.
        if (phoneAuthViewModel.authCode.value.isNotEmpty()) {
            phoneAuthViewModel.getAuthResult().addOnCompleteListener(requireActivity()) { task ->
                handleAuthResult(task)
            }.addOnFailureListener {
                alertDialogWithButton(getString(R.string.phoneauth_invalid_verification_code_alert))
            }
        }
    }

//  alertDialog class 나중에 빼기 일단 보류
    private fun alertDialogWithButton(alertText: String) {
        val builder = AlertDialog.Builder(requireContext())
            .setCancelable(false)
        val alertDialog: AlertDialog = builder.create()
        val dialogLayout = layoutInflater.inflate(R.layout.alert_dialog_with_button, null)

        val textViewMessage = dialogLayout.findViewById<TextView>(R.id.alert_message)
        val alertButton = dialogLayout.findViewById<TextView>(R.id.alert_button)

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        textViewMessage.text = alertText

        alertButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.setView(dialogLayout)
        alertDialog.show()
        val width = resources.getDimensionPixelSize(R.dimen.alert_dialog_width)
        val height = resources.getDimensionPixelSize(R.dimen.alert_dialog_height)
        alertDialog.window?.setLayout(width, height)
    }

    companion object {
        const val EXPIRE_TIME = 120L
    }
}
