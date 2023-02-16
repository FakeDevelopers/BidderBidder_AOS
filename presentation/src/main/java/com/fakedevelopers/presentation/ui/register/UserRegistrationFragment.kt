package com.fakedevelopers.presentation.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentUserRegistrationBinding
import com.fakedevelopers.presentation.ui.MainActivity
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import kotlinx.coroutines.flow.collectLatest

class UserRegistrationFragment : BaseFragment<FragmentUserRegistrationBinding>(
    R.layout.fragment_user_registration
) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        setProgressBar(viewModel.currentStep)
        viewModel.setInitialStep()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    override fun initListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            setToolbarTitleByDestination(destination.id)
            setNextByDestination(destination.id)
        }
        // 뒤로버튼
        binding.includeUserRegistrationToolbar.buttonBack.setOnClickListener {
            viewModel.toPreviousStep()
        }
    }

    private fun setRegistrationNextButton(state: Boolean) {
        binding.buttonUserRegistrationNext.let {
            val color = if (state) R.color.bidderbidder_primary else R.color.bidderbidder_gray
            it.isEnabled = state
            it.setBackgroundColor(ContextCompat.getColor(requireActivity(), color))
        }
    }

    override fun initCollector() {
        // 회원가입 단계 관리
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.changeRegistrationStep.collectLatest {
                toNextStep(it)
            }
        }
        // 실패 토스트 메세지
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.failureMessage.collectLatest {
                sendSnackBar(it)
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.nextStepEnabled.collect {
                setRegistrationNextButton(it)
            }
        }
    }

    private fun setToolbarTitleByDestination(destinationId: Int) {
        binding.includeUserRegistrationToolbar.textviewTitle.run {
            when (destinationId) {
                R.id.acceptTermsFragment -> setText(R.string.registration_toolbar_accept_terms)
                R.id.acceptTermsFragmentContents -> setText(R.string.registration_toolbar_accept_term_detail)
                else -> setText(R.string.registration_toolbar_user_registration)
            }
        }
    }

    private fun setNextByDestination(destinationId: Int) {
        binding.buttonUserRegistrationNext.visibility = when (destinationId) {
            R.id.acceptTermsFragmentContents -> View.INVISIBLE
            else -> View.VISIBLE
        }
    }

    private fun setProgressBar(state: RegistrationProgressState) {
        binding.includeUserRegistrationNavigation.run {
            root.visibility = state.getVisibleState()
            registrationProgressbar.progress = state.getProgressPercentage()
        }
    }

    // 다음 단계 네비게이션
    private fun toNextStep(state: RegistrationProgressState) {
        NavOptions.Builder().setLaunchSingleTop(true)
        viewModel.setNextStepEnabled(false)
        setProgressBar(state)

        if (state.checkCancelStep()) {
            findNavController().popBackStack()
        }

        if (state.checkLastStep()) {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        state.navigationId?.let { navId ->
            navigate(navId)
        }
    }

    private fun navigate(id: Int) {
        navController.navigate(id, null, singleTopOptions)
    }

    override fun onDestroy() {
        backPressedCallback.remove()
        super.onDestroy()
    }
}
