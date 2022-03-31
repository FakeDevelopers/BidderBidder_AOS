package com.fakedevelopers.ddangddangmarket.ui.login_type

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fakedevelopers.ddangddangmarket.FragmentList
import com.fakedevelopers.ddangddangmarket.MainActivity
import com.fakedevelopers.ddangddangmarket.databinding.FragmentLoginTypeBinding

class LoginTypeFragment : Fragment() {

    private var _binding: FragmentLoginTypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        // 로그인 버튼을 누르면 로그인 프래그먼트로 넘어갑니다.
        binding.buttonLogintypeCommonlogin.setOnClickListener {
            mainActivity.setFragment(FragmentList.LOGIN)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
