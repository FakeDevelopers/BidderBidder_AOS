package com.fakedevelopers.presentation.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.api.data.Constants.Companion.LOGIN_SUCCESS
import com.fakedevelopers.presentation.databinding.FragmentLoginBinding
import com.fakedevelopers.presentation.ui.MainActivity
import com.fakedevelopers.presentation.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        // 로그인 버튼
        binding.includeLoginSignin.button.setOnClickListener {
            viewModel.loginRequest()
        }
        // 회원가입 버튼
        binding.textviewLoginRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_userRegistrationFragment)
        }
    }

    private fun initCollector() {
        // 결과 처리
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResponse.collect {
                    if (it.isSuccessful && it.body().toString() == LOGIN_SUCCESS) {
                        navigateActivity(MainActivity::class.java)
                    } else {
                        sendSnackBar(it.errorBody().toString())
                    }
                }
            }
        }
    }
}
