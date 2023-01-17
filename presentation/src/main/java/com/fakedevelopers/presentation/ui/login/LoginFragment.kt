package com.fakedevelopers.presentation.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.domain.secret.Constants.Companion.LOGIN_SUCCESS
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentLoginBinding
import com.fakedevelopers.presentation.ui.MainActivity
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.logging.helper.stringify
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(
    R.layout.fragment_login
) {

    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        initListener()
        initCollector()
    }

    private fun initListener() {
        binding.includeLoginSignin.button.setOnClickListener {
            viewModel.loginRequest()
        }
        binding.textviewLoginRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_userRegistrationFragment)
        }
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.loginEvent.collectLatest { result ->
                if (result.isSuccess && result.getOrThrow() == LOGIN_SUCCESS) {
                    navigateActivity(MainActivity::class.java)
                } else {
                    sendSnackBar(result.exceptionOrNull()?.stringify().toString())
                }
            }
        }
    }
}
