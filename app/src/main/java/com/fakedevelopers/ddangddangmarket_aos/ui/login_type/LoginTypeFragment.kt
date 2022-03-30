package com.fakedevelopers.ddangddangmarket_aos.ui.login_type

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fakedevelopers.ddangddangmarket_aos.MainActivity
import com.fakedevelopers.ddangddangmarket_aos.R
import kotlinx.android.synthetic.main.fragment_login_type.*

class LoginTypeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as MainActivity
        // 로그인 버튼을 누르면 로그인 프래그먼트로 넘어갑니다.
        button_logintype_commonlogin.setOnClickListener {
            mainActivity.setFragment("Login")
        }
    }
}