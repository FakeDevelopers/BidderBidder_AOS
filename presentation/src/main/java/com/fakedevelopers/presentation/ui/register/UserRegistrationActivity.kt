package com.fakedevelopers.presentation.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.ActivityUserRegistrationBinding
import com.fakedevelopers.presentation.ui.MainActivity
import com.fakedevelopers.presentation.ui.base.BaseActivity
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class UserRegistrationActivity : BaseActivity<ActivityUserRegistrationBinding>(
    ActivityUserRegistrationBinding::inflate
) {
    private val viewModel: UserRegistrationViewModel by viewModels()
    private lateinit var navController: NavController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
        navController = (supportFragmentManager.findFragmentById(R.id.navigation_user_registration) as NavHostFragment).navController
        setProgressBar(viewModel.currentStep)
        viewModel.setInitialStep()
        initListener()
        initCollector()
    }

    override fun onStart() {
        super.onStart()
        onBackPressedDispatcher.addCallback(backPressedCallback)
    }

    fun initListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            setToolbarTitleByDestination(destination.id)
            setNextByDestination(destination.id)
        }
        // 뒤로버튼
        binding.includeUserRegistrationToolbar.buttonBack.setOnClickListener {
            viewModel.toPreviousStep()
        }
    }

    fun initCollector() {
        // 회원가입 단계 관리
        repeatOnStarted(this) {
            viewModel.changeRegistrationStep.collectLatest {
                toNextStep(it)
            }
        }
        // 실패 토스트 메세지
        repeatOnStarted(this) {
            viewModel.failureMessage.collectLatest {
                // sendSnackBar(it)
            }
        }
        repeatOnStarted(this) {
            viewModel.nextStepEnabled.collect {
                binding.buttonUserRegistrationNext.isEnabled = it
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
            navController.popBackStack()
        }

        if (state.checkLastStep()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
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
