package com.fakedevelopers.bidderbidder.ui.register.id

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
    }

    private fun initListener() {
        binding.buttonIdDuplicationCheck.setOnClickListener {
            viewModel.isUserIdDuplicated()
        }
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userIdDuplicationState.collectLatest {
                    binding.textviewIdDuplicationInfo.visibility = View.VISIBLE
                    if (it) {
                        setDuplicationInfo(R.string.registration_id_is_duplicated, R.color.alert_red)
                    } else {
                        setDuplicationInfo(R.string.registration_id_is_ok, R.color.bidderbidder_primary)
                    }
                }
            }
        }
    }

    // 중복 체크 표시
    private fun setDuplicationInfo(stringId: Int, colorId: Int) {
        binding.textviewIdDuplicationInfo.apply {
            setText(stringId)
            setTextColor(ContextCompat.getColor(requireContext(), colorId))
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
