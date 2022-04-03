package com.fakedevelopers.ddangddangmarket.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.ddangddangmarket.FragmentType
import com.fakedevelopers.ddangddangmarket.MainActivity
import com.fakedevelopers.ddangddangmarket.api.MainViewModel
import com.fakedevelopers.ddangddangmarket.api.MainViewModelFactory
import com.fakedevelopers.ddangddangmarket.api.repository.Repository
import com.fakedevelopers.ddangddangmarket.databinding.FragmentLoginBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.BuildConfig
import com.orhanobut.logger.Logger

class LoginFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 버튼 클릭 시 로그인 확인 API를 호출
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.addLogAdapter(AndroidLogAdapter())

        binding.buttonLoginSignin.setOnClickListener {
            // 리포지토리와 ViewModel 연결
            val repository = Repository()
            val viewModelFactory = MainViewModelFactory(repository)
            // 다른 ViewModel을 사용할거면 클래스만 바꾸면 된다.
            viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
            // api 요청
            viewModel.loginRequest(binding.inputLoginEmail.text.toString(), binding.inputLoginPassword.text.toString())
            // 결과 처리
            viewModel.loginResponse.observe(viewLifecycleOwner) {
                if(it.isSuccessful){
                    Logger.t("Login").i(it.body().toString())
                    if(it.body().toString() == "success"){
                        val mainActivity = activity as MainActivity
                        mainActivity.setFragment(FragmentType.MAIN)
                    }
                } else {
                    Logger.e(it.errorBody().toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
