package com.fakedevelopers.presentation.ui.register.password

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentUserRegistrationPasswordBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.register.UserRegistrationViewModel
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import kotlinx.coroutines.flow.collectLatest

class UserRegistrationPasswordFragment : BaseFragment<FragmentUserRegistrationPasswordBinding>(
    R.layout.fragment_user_registration_password
) {
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        if (viewModel.userPasswordConditionLengthState.value &&
            viewModel.userPasswordConditionAlphabetState.value &&
            viewModel.userPasswordConditionAlphabetState.value
        ) {
            binding.textviewPasswordConfirmInfo.visibility = View.VISIBLE
        }
    }

    override fun initListener() {
        // 만료 시간 필터 등록
        binding.run {
            edittextPassword.setOnFocusChangeListener { _, hasFocus ->
                passwordClearButton.visibility = getVisibility(
                    hasFocus && edittextPassword.text.isNullOrBlank().not()
                )
            }
            passwordClearButton.setOnClickListener {
                edittextPassword.text?.clear()
                passwordClearButton.visibility = View.GONE
            }
            passwordPasswordToggle.setOnClickListener {
                edittextPassword.transformationMethod = if (passwordPasswordToggle.isChecked) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }
            }
            edittextPasswordConfirm.setOnFocusChangeListener { _, hasFocus ->
                passwordConfirmClearButton.visibility = getVisibility(
                    hasFocus && edittextPasswordConfirm.text.isNullOrBlank().not()
                )
            }
            passwordConfirmClearButton.setOnClickListener {
                edittextPasswordConfirm.text?.clear()
                passwordConfirmClearButton.visibility = View.GONE
            }
            passwordConfirmPasswordToggle.setOnClickListener {
                edittextPasswordConfirm.transformationMethod = if (passwordConfirmPasswordToggle.isChecked) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }
            }
        }
    }

    override fun initCollector() {
        // 비밀번호 길이 조건
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userPasswordConditionLengthState.collectLatest {
                setTextViewColor(binding.textviewPasswordConditionLength, getColorId(it))
            }
        }
        // 비밀번호 문자 조건
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userPasswordConditionAlphabetState.collectLatest {
                setTextViewColor(binding.textviewPasswordConditionAlphabet, getColorId(it))
            }
        }
        // 비밀번호 숫자 조건
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userPasswordConditionNumberState.collectLatest {
                setTextViewColor(binding.textviewPasswordConditionNumber, getColorId(it))
            }
        }
        // 비밀번호 확인 visible
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userPasswordConfirmVisible.collectLatest {
                binding.textviewPasswordConfirmInfo.visibility = getVisibility(it)
            }
        }
        // 비밀번호 확인 텍스트
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userPasswordConfirmState.collectLatest {
                setPasswordConfirmInfo(it)
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.inputUserPassword.collectLatest {
                checkPasswordsSame()
                binding.apply {
                    if (it.isBlank()) {
                        passwordPasswordToggle.visibility = View.GONE
                    } else {
                        passwordPasswordToggle.visibility = View.VISIBLE
                        passwordClearButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.inputConfirmUserPassword.collectLatest {
                checkPasswordsSame()
                binding.apply {
                    if (it.isBlank()) {
                        passwordConfirmPasswordToggle.visibility = View.GONE
                    } else {
                        passwordConfirmPasswordToggle.visibility = View.VISIBLE
                        passwordConfirmClearButton.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun getVisibility(state: Boolean) = if (state) View.VISIBLE else View.INVISIBLE

    // 두 비밀번호가 같으면 색 파란색 다르면 빨간색으로 설정
    private fun checkPasswordsSame() {
        viewModel.apply {
            if (inputConfirmUserPassword.value.contentEquals(inputUserPassword.value)) {
                setEditPasswordConfirmBackground(R.drawable.text_input_white_background_accepted)
            } else {
                setEditPasswordConfirmBackground(R.drawable.text_input_white_background_error)
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
}
