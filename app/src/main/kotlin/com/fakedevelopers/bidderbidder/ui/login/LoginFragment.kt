package com.fakedevelopers.bidderbidder.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.LoginViewModel
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.LOGIN_SUCCESS
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<FragmentLoginBinding?>(inflater, R.layout.fragment_login, container, false).also {
            it.vm = loginViewModel
            it.lifecycleOwner = this
        }
        return binding.root
    }

    // 버튼 클릭 시 로그인 확인 API를 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)
        Logger.addLogAdapter(AndroidLogAdapter())
        // 로그인 버튼
        binding.buttonLoginSignin.setOnClickListener {
            with(loginViewModel){
                // api 요청
                loginRequest()
            }
        }
        // 회원가입 버튼
        binding.buttonLoginResgister.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_phoneAuthFragment)
        }

        // 결과 처리
        loginViewModel.loginResponse.observe(viewLifecycleOwner) {
            if(it.isSuccessful){
                Logger.t("Login").i(it.body().toString())
                if(it.body().toString() == LOGIN_SUCCESS){
                    navController.navigate(R.id.action_loginFragment_to_mainFragment)
                }
            } else {
                Logger.e(it.errorBody().toString())
            }
        }
    }
}