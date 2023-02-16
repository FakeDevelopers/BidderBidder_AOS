package com.fakedevelopers.presentation.ui.register.id

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentUserRegistrationIdBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.register.UserRegistrationViewModel
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import kotlinx.coroutines.flow.collectLatest

class UserRegistrationIdFragment : BaseFragment<FragmentUserRegistrationIdBinding>(
    R.layout.fragment_user_registration_id
) {
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        if (!viewModel.lastDuplicationState) {
            setDuplicationInfo(R.string.registration_id_is_ok, R.color.bidderbidder_primary, true)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initListener() {
        binding.apply {
            edittextId.setOnFocusChangeListener { _, hasFocus ->
                clearButton.visibility = if (hasFocus) View.VISIBLE else View.GONE
            }
            clearButton.setOnTouchListener { _, _ ->
                edittextId.text?.clear()
                true
            }
        }
    }

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userIdValidationState.collectLatest {
                if (it) {
                    setDuplicationInfo(R.string.registration_id_is_Invalid, R.color.alert_red, false)
                    setTextInputBackground(R.drawable.text_input_white_background_error)
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.userIdDuplicationState.collectLatest {
                if (it) {
                    setDuplicationInfo(R.string.registration_id_is_duplicated, R.color.alert_red, false)
                    setTextInputBackground(R.drawable.text_input_white_background_error)
                } else {
                    setDuplicationInfo(R.string.registration_id_is_ok, R.color.bidderbidder_primary, true)
                    setTextInputBackground(R.drawable.text_input_white_background_accepted)
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.inputUserId.collectLatest {
                binding.textviewIdDuplicationInfo.visibility = View.INVISIBLE
                setTextInputBackground(R.drawable.text_input_white_background_normal)
                binding.clearButton.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
            }
        }
    }

    // 중복 체크 표시
    private fun setDuplicationInfo(stringId: Int, colorId: Int, state: Boolean) {
        binding.textviewIdDuplicationInfo.apply {
            visibility = if (viewModel.inputUserId.value.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            setText(stringId)
            setTextColor(ContextCompat.getColor(requireContext(), colorId))
            this.isSelected = state
        }
    }

    private fun setTextInputBackground(drawableId: Int) {
        binding.edittextId.background =
            ContextCompat.getDrawable(requireContext(), drawableId)
    }
}
