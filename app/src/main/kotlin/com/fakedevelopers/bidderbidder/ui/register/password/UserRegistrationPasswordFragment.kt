package com.fakedevelopers.bidderbidder.ui.register.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        initCollector()
        if (viewModel.userPasswordConditionLengthState.value && viewModel.userPasswordConditionCharacterState.value) {
            binding.textviewPasswordConfirmInfo.visibility = View.VISIBLE
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
                viewModel.userPasswordConditionCharacterState.collectLatest {
                    setTextViewColor(binding.textviewPasswordConditionCharacter, getColorId(it))
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.inputConfirmUserPassword.collectLatest {
                    if (it.isNotEmpty()) {
                        if (it == viewModel.inputUserPassword.value) {
                            setEditPasswordConfirmBackground(R.drawable.text_input_white_background)
                        } else {
                            setEditPasswordConfirmBackground(R.drawable.text_input_white_background_error)
                        }
                    }
                }
            }
        }
    }

    private fun setPasswordConfirmInfo(state: Boolean) {
        binding.textviewPasswordConfirmInfo.let {
            if (state) {
                it.setText(R.string.registration_password_is_same)
                it.onCheckIsTextEditor()
                it.isSelected = true
            } else {
                it.setText(R.string.registration_password_is_not_same)
                it.isSelected = false
            }
            setTextViewColor(it, getColorId(state))
        }
    }

    private fun setEditPasswordConfirmBackground(drawableId: Int) {
        binding.edittextPasswordConfirm.background = ContextCompat.getDrawable(requireContext(), drawableId)
    }

    private fun setTextViewColor(tv: TextView, colorId: Int) {
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorId))
        tv.isSelected = colorId == R.color.bidderbidder_primary
    }

    private fun getColorId(state: Boolean) = if (state) R.color.bidderbidder_primary else R.color.alert_red

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
