package com.fakedevelopers.bidderbidder.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentUserRegistrationBinding
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.ACCEPT_TERMS
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.CONGRATULATIONS
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_BIRTH
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_ID
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.INPUT_PASSWORD
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.PHONE_AUTH_BEFORE_SENDING
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserRegistrationFragment : Fragment() {

    private var _binding: FragmentUserRegistrationBinding? = null

    private val binding get() = _binding!!
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }
    private val navController by lazy {
        (childFragmentManager.findFragmentById(R.id.navigation_user_registration) as NavHostFragment).navController
    }
    private val singleTopOptions = navOptions {
        launchSingleTop = true
    }
    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.toPreviousStep()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_registration,
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

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    private fun initListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            setToolbarTitleByDestination(destination.id)
        }
        // 뒤로버튼
        binding.includeUserRegistrationToolbar.buttonBack.setOnClickListener {
            viewModel.toPreviousStep()
        }
    }

    private fun initCollector() {
        // 회원가입 단계 관리
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changeRegistrationStep.collectLatest {
                    toNextStep(it)
                }
            }
        }
        // 실패 토스트 메세지
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.failureMessage.collectLatest {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setToolbarTitleByDestination(destinationId: Int) {
        binding.includeUserRegistrationToolbar.textviewTitle.apply {
            when (destinationId) {
                R.id.acceptTermsFragment -> setText(R.string.registration_toolbar_accept_terms)
                else -> setText(R.string.registration_toolbar_user_registration)
            }
        }
    }

    // 다음 단계 네비게이션
    private fun toNextStep(state: RegistrationProgressState) {
        NavOptions.Builder().setLaunchSingleTop(true)
        when (state) {
            ACCEPT_TERMS -> navigate(R.id.acceptTermsFragment)
            PHONE_AUTH_BEFORE_SENDING -> navigate(R.id.phoneAuthFragment)
            INPUT_BIRTH -> navigate(R.id.userRegistrationBirthFragment)
            INPUT_ID -> navigate(R.id.userRegistrationIdFragment)
            INPUT_PASSWORD -> navigate(R.id.userRegistrationPasswordFragment)
            CONGRATULATIONS -> findNavController().navigate(R.id.action_userRegistrationFragment_to_productListFragment)
            else -> {
                // 여긴 아무것도 안해!
            }
        }
    }

    private fun navigate(id: Int) {
        navController.navigate(id, null, singleTopOptions)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        backPressedCallback.remove()
    }
}
