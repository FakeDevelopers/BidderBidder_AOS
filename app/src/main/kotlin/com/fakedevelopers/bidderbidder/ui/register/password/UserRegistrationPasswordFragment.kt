package com.fakedevelopers.bidderbidder.ui.register.password

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentUserRegistrationPasswordBinding
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserRegistrationPasswordFragment : Fragment() {

    private var _binding: FragmentUserRegistrationPasswordBinding? = null

    private val binding get() = _binding!!
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_registration_password,
            container,
            false
        )
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initCollector()
        if (viewModel.userPasswordConditionLengthState.value &&
            viewModel.userPasswordConditionAlphabetState.value &&
            viewModel.userPasswordConditionAlphabetState.value
        ) {
            binding.textviewPasswordConfirmInfo.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        // 만료 시간 필터 등록
        binding.apply {
            edittextPassword.addTextChangedListener() {
                Logger.i(edittextPassword.text.toString())
                if (it.contentEquals(edittextPasswordConfirm.text)) {
                    setEditPasswordConfirmBackground(R.drawable.text_input_white_background_accepted)
                } else {
                    setEditPasswordConfirmBackground(R.drawable.text_input_white_background_normal)
                }
                if (it.isNullOrBlank()) {
                    passwordPasswordToggle.visibility = View.GONE
                } else {
                    passwordClearButton.visibility = View.VISIBLE
                    passwordPasswordToggle.visibility = View.VISIBLE
                }
            }
            edittextPassword.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (edittextPassword.text.isNullOrBlank()) {
                        passwordClearButton.visibility = View.GONE
                    } else {
                        passwordClearButton.visibility = View.VISIBLE
                    }
                } else {
                    passwordClearButton.visibility = View.GONE
                }
            }
            passwordClearButton.setOnTouchListener { _, _ ->
                edittextPassword.text?.clear()
                passwordClearButton.visibility = View.GONE
                true
            }
            passwordPasswordToggle.setOnClickListener {
                if (passwordPasswordToggle.isChecked) {
                    edittextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    edittextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
            edittextPasswordConfirm.addTextChangedListener() {
                if (it.contentEquals(edittextPassword.text)) {
                    setEditPasswordConfirmBackground(R.drawable.text_input_white_background_accepted)
                } else {
                    setEditPasswordConfirmBackground(R.drawable.text_input_white_background_error)
                }
                if (it.isNullOrBlank()) {
                    passwordConfirmPasswordToggle.visibility = View.GONE
                } else {
                    passwordConfirmClearButton.visibility = View.VISIBLE
                    passwordConfirmPasswordToggle.visibility = View.VISIBLE
                }
            }

            edittextPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (edittextPasswordConfirm.text.isNullOrBlank()) {
                        passwordConfirmClearButton.visibility = View.GONE
                    } else {
                        passwordConfirmClearButton.visibility = View.VISIBLE
                    }
                } else {
                    passwordConfirmClearButton.visibility = View.GONE
                }
            }
            passwordConfirmClearButton.setOnTouchListener { _, _ ->
                edittextPasswordConfirm.text?.clear()
                passwordConfirmClearButton.visibility = View.GONE
                true
            }
            passwordConfirmPasswordToggle.setOnClickListener {
                if (passwordConfirmPasswordToggle.isChecked) {
                    edittextPasswordConfirm.transformationMethod = HideReturnsTransformationMethod.getInstance()
                } else {
                    edittextPasswordConfirm.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
        }
    }

    private fun initCollector() {
        // 비밀번호 길이 조건
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPasswordConditionLengthState.collectLatest {
                    setTextViewColor(binding.textviewPasswordConditionLength, getColorId(it))
                }
            }
        }
        // 비밀번호 문자 조건
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPasswordConditionAlphabetState.collectLatest {
                    setTextViewColor(binding.textviewPasswordConditionAlphabet, getColorId(it))
                }
            }
        }
        // 비밀번호 숫자 조건
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPasswordConditionNumberState.collectLatest {
                    setTextViewColor(binding.textviewPasswordConditionNumber, getColorId(it))
                }
            }
        }
        // 비밀번호 확인 visible
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPasswordConfirmVisible.collectLatest {
                    binding.textviewPasswordConfirmInfo.visibility = if (it) View.VISIBLE else View.INVISIBLE
                }
            }
        }
        // 비밀번호 확인 텍스트
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPasswordConfirmState.collectLatest {
                    setPasswordConfirmInfo(it)
                }
            }
        }
    }

    private fun setPasswordConfirmInfo(state: Boolean) {
        binding.textviewPasswordConfirmInfo.let {
            it.isSelected = state
            if (state) {
                it.setText(R.string.registration_password_is_same)
                it.onCheckIsTextEditor()
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.bidderbidder_primary))
            } else {
                it.setText(R.string.registration_password_is_not_same)
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.alert_red))
            }
        }
    }

    private fun setEditPasswordConfirmBackground(drawableId: Int) {
        binding.edittextPasswordConfirm.background = ContextCompat.getDrawable(requireContext(), drawableId)
    }

    private fun setTextViewColor(tv: TextView, colorId: Int) {
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorId))
        tv.isSelected = colorId == R.color.bidderbidder_primary
        tv.isEnabled = viewModel.inputUserPassword.value.isNotEmpty()
    }
    private fun getColorId(state: Boolean) = if (state) R.color.bidderbidder_primary else R.color.edit_text_hint

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
