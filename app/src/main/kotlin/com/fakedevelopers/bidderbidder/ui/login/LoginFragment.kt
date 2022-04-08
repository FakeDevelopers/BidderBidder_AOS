package com.fakedevelopers.bidderbidder.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.bidderbidder.FragmentType
import com.fakedevelopers.bidderbidder.MainActivity
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.LoginViewModel
import com.fakedevelopers.bidderbidder.api.MainViewModelFactory
import com.fakedevelopers.bidderbidder.api.repository.LoginRepository
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var mainActivity: MainActivity
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory(LoginRepository()))[LoginViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        mainActivity = activity as MainActivity
        binding = DataBindingUtil.inflate<FragmentLoginBinding?>(inflater, R.layout.fragment_login, container, false).also {
            it.vm = loginViewModel
            it.lifecycleOwner = this
        }
        // 결과 처리
        loginViewModel.loginResponse.observe(viewLifecycleOwner) {
            if(it.isSuccessful){
                Logger.t("Login").i(it.body().toString())
                if(it.body().toString() == "success"){
                    mainActivity.setFragment(FragmentType.MAIN)
                }
            } else {
                Logger.e(it.errorBody().toString())
            }
        }
        return binding.root
    }

    // 버튼 클릭 시 로그인 확인 API를 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.addLogAdapter(AndroidLogAdapter())
        // 로그인 버튼
        binding.buttonLoginSignin.setOnClickListener {
            with(loginViewModel){
                // api 요청
                loginRequest(email.value!!, passwd.value!!)
            }
        }
        // 회원가입 버튼
        binding.buttonLoginResgister.setOnClickListener {
            mainActivity.setFragment(FragmentType.PHONEAUTH)
        }
    }
}
