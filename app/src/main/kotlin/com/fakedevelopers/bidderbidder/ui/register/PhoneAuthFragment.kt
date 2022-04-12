package com.fakedevelopers.bidderbidder.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.bidderbidder.MainActivity
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentPhoneAuthBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class PhoneAuthFragment : Fragment() {

    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentPhoneAuthBinding
    private val binding get() = _binding
    private val phoneAuthViewModel: PhoneAuthViewModel by lazy {
        ViewModelProvider(this)[PhoneAuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        mainActivity = activity as MainActivity
        _binding = DataBindingUtil.inflate<FragmentPhoneAuthBinding>(inflater, R.layout.fragment_phone_auth, container, false).also {
            // 뷰 모델과 데이터 바인딩 합체
            it.vm = phoneAuthViewModel
            it.lifecycleOwner = this
        }
        initObserver()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 인증 번호 발송 버튼
        binding.buttonPhoneauthNextstep.setOnClickListener {
            with(phoneAuthViewModel){
                if(isCodeSending.value!!){
                    // 인증 번호 확인
                    signInWithPhoneAuthCredential(mainActivity, view)
                } else {
                    // 인증 번호 전송
                    sendPhoneAuthCode(mainActivity)
                }
            }
        }
    }

    private fun initObserver() {
        // 코드 발송 상태에 따라 버튼 메세지가 바뀜
        phoneAuthViewModel.isCodeSending.observe(viewLifecycleOwner) {
            with(binding){
                if(it) {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_nextstep)
                    textinputlayoutPhoneauthAuthcode.visibility = View.VISIBLE
                }
                else {
                    buttonPhoneauthNextstep.setText(R.string.phoneauth_getauthcode)
                    textinputlayoutPhoneauthAuthcode.visibility = View.INVISIBLE
                }
                edittextPhoneauthAuthcode.isEnabled = it
            }
        }
    }

    // SafetyNet 사용가능 여부
    // SafetyNet이 없으면 휴대폰 인증을 받기전에 리캡챠가 뜹니다.
    // 아직은 사용하지 않읍니다
    private fun isSafetyNetAvailable(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS
    }
}
