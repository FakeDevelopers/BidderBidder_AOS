package com.fakedevelopers.bidderbidder.ui.register.phone_auth

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentPhoneAuthBinding
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_BIRTH
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_CHECK_AUTH_CODE
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@AndroidEntryPoint
class PhoneAuthFragment : Fragment() {

    private var _binding: FragmentPhoneAuthBinding? = null

    private val binding get() = _binding!!
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
                Toast.makeText(requireContext(), "저런 실패했군요!", Toast.LENGTH_SHORT).show()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_phone_auth,
            container,
            false
        )
        return binding.run {
            vm = phoneAuthViewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initCollector()
    }

    private fun initListener() {
        // 인증 번호 발송 버튼
        binding.edittextRegisterPhone.addTextChangedListener {
            setPhoneValidInfo(R.string.phoneauth_number_is_valid, R.color.bidderbidder_primary, true)
            setTextInputBackground(R.drawable.text_input_white_background_normal)
            if (isPhoneNumberCheck(it.toString())) {
                binding.buttonPhoneauthSendCode.setText(R.string.phoneauth_getauthcode)
                setButtonTextColor(R.color.white)
                setButtonBackground(R.drawable.button_phone_auth_before_send_ready)
            } else {
                setButtonTextColor(R.color.black)
                setButtonBackground(R.drawable.button_phone_auth_before_send)
            }
        }
        binding.buttonPhoneauthSendCode.setOnClickListener {
            if (phoneAuthViewModel.phoneNumber.value.isNotEmpty()) {
                if (isPhoneNumberCheck(phoneAuthViewModel.phoneNumber.value)) {
                    runFadeInAlertBox()
                    Handler(Looper.getMainLooper()).postDelayed({
                        runFadeOutAlertBox()
                    }, 1000)
                    sendPhoneAuthCode()
                    setPhoneValidInfo(R.string.phoneauth_number_is_valid, R.color.bidderbidder_primary, true)
                    setTextInputBackground(R.drawable.text_input_white_background_normal)
                } else {
                    setPhoneValidInfo(R.string.phoneauth_number_is_invalid, R.color.alert_red, false)
                    setTextInputBackground(R.drawable.text_input_white_background_error)
                }
            }
        }
        // 인증 번호 6자리 되면 다음 버튼 활성화
        binding.edittextPhoneauthAuthcode.addTextChangedListener() {
            userRegistrationViewModel.setAuthCode(it.toString())
        }
    }

    private fun initCollector() {
        // 코드 발송 상태에 따라 버튼 메세지가 바뀜
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                phoneAuthViewModel.codeSendingStates.collectLatest { state ->
                    handlePhoneAuthEvent(state)
                }
            }
        }
        // 인증 코드 검사 요청
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userRegistrationViewModel.checkAuthCode.collectLatest {
                    signInWithPhoneAuthCredential()
                }
            }
        }
        // 타이머 visibility
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                phoneAuthViewModel.timerVisibility.collectLatest {
                    binding.textviewPhoneauthTimer.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    private fun runFadeInAlertBox() {
        binding.includeRegistrationAlert.root.let {
            it.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
            // starting the animation
            it.startAnimation(animation)
        }
    }

    private fun runFadeOutAlertBox() {
        binding.includeRegistrationAlert.root.let {
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
            it.startAnimation(animation)
            it.visibility = View.INVISIBLE
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

    private fun isPhoneNumberCheck(cellphoneNumber: String): Boolean {
        var returnValue = false
        val regex = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$"
        val p = Pattern.compile(regex)

        val m = p.matcher(cellphoneNumber)

        if (m.matches()) {
            returnValue = true
        }

        return returnValue
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
                        setCurrentStep(INPUT_BIRTH)
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
                val e = it as FirebaseAuthException
                Toast.makeText(requireContext(), getAuthErrorMessage(e), Toast.LENGTH_SHORT).show()
                alertDialogWithButton()
            }
        }
    }

    private fun alertDialogWithButton() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.alert_dialog_with_button, null)

        val textViewMessage = dialogLayout.findViewById<TextView>(R.id.alert_message)
        textViewMessage.text = getString(R.string.phoneauth_invalid_verification_code_message)
        builder.setView(dialogLayout)

        builder.show()
    }

    // 에러 메세지 추출
    private fun getAuthErrorMessage(e: FirebaseAuthException) =
        when (e.errorCode) {
            getString(R.string.phoneauth_invalid_verification_code_type) ->
                getString(R.string.phoneauth_invalid_verification_code_message)
            getString(R.string.phoneauth_session_expired_type) ->
                getString(R.string.phoneauth_session_expired_message)
            else -> e.errorCode
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val EXPIRE_TIME = 120L
    }
}
