package com.fakedevelopers.bidderbidder.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.LOGIN_SUCCESS
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginBinding
import com.fakedevelopers.bidderbidder.ui.MainActivity
import com.fakedevelopers.bidderbidder.ui.base.BaseFragment
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
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        sendSnackBar(it.errorBody().toString())
                    }
                }
            }
        }
    }
}
