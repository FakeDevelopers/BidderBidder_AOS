package com.fakedevelopers.bidderbidder.ui.register.id

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
        if (!viewModel.getIdDuplicationState()) {
            setDuplicationInfo(R.string.registration_id_is_ok, R.color.bidderbidder_primary, true)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        binding.apply {
            edittextId.addTextChangedListener() {
                textviewIdDuplicationInfo.visibility = View.INVISIBLE
                setTextInputBackground(R.drawable.text_input_white_background_normal)
                if (it.isNullOrBlank()) {
                    clearButton.visibility = View.GONE
                } else {
                    clearButton.visibility = View.VISIBLE
                }
            }
            edittextId.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    clearButton.visibility = View.VISIBLE
                    // DO NOTHING
                } else {
                    clearButton.visibility = View.GONE
                }
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

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
