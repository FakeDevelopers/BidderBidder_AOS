package com.fakedevelopers.bidderbidder.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.LOGIN_SUCCESS
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val binding: FragmentLoginBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Logger.addLogAdapter(AndroidLogAdapter())
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    // 버튼 클릭 시 로그인 확인 API 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
        initCollector()
    }

    private fun initListener() {
        // 로그인 버튼
        binding.buttonLoginSignin.setOnClickListener {
            viewModel.loginRequest()
        }
        // 회원가입 버튼
        binding.buttonLoginResgister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_phoneAuthFragment)
        }
    }

    private fun initCollector() {
        // 결과 처리
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginResponse.collect {
                    if (it.isSuccessful) {
                        Logger.t("Login").i(it.body().toString())
                        if (it.body().toString() == LOGIN_SUCCESS) {
                            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                        }
                    } else {
                        Logger.e(it.errorBody().toString())
                    }
                }
            }
        }
    }
}
