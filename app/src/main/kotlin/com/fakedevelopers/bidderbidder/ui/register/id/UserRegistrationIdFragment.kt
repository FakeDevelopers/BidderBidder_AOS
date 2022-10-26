package com.fakedevelopers.bidderbidder.ui.register.id

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentUserRegistrationIdBinding
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserRegistrationIdFragment : Fragment() {

    private var _binding: FragmentUserRegistrationIdBinding? = null

    private val binding get() = _binding!!
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_registration_id,
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
        if (!viewModel.lastDuplicationState) {
            setDuplicationInfo(R.string.registration_id_is_ok, R.color.bidderbidder_primary, true)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
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
    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userIdValidationState.collectLatest {
                    if (it) {
                        setDuplicationInfo(R.string.registration_id_is_Invalid, R.color.alert_red, false)
                        setTextInputBackground(R.drawable.text_input_white_background_error)
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apply {
                    inputUserId.collectLatest {
                        binding.textviewIdDuplicationInfo.visibility = View.INVISIBLE
                        setTextInputBackground(R.drawable.text_input_white_background_normal)
                        binding.clearButton.visibility = if (it.isBlank()) View.GONE else View.VISIBLE
                    }
                }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
