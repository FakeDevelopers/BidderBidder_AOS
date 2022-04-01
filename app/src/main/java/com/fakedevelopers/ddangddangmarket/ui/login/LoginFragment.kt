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
        binding.buttonLoginSignin.setOnClickListener {
            // 리포지토리와 ViewModel 연결
            val repository = Repository()
            val viewModelFactory = MainViewModelFactory(repository)
            viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
            // api 요청
            viewModel.loginRequest(binding.inputLoginEmail.text.toString(), binding.inputLoginPassword.text.toString())
            // 결과 처리
            viewModel.loginResponse.observe(viewLifecycleOwner) {
                if(it.isSuccessful){
                    Log.d("Login", it.body().toString())
                    if(it.body().toString() == "success"){
                        val mainActivity = activity as MainActivity
                        mainActivity.setFragment(FragmentType.MAIN)
                    }
                } else {
                    Log.d("Login", it.errorBody().toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
