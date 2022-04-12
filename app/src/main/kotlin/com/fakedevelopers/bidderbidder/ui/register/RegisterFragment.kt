package com.fakedevelopers.bidderbidder.ui.register

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentRegisterBinding
import com.orhanobut.logger.Logger

class RegisterFragment : Fragment() {

    private lateinit var _binding: FragmentRegisterBinding
    private val binding get() = _binding
    private val registerViewModel: RegisterViewModel by lazy {
        ViewModelProvider(this)[RegisterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate<FragmentRegisterBinding>(inflater, R.layout.fragment_register, container, false).also {
            it.vm = registerViewModel
            it.lifecycleOwner = this
        }
        initObserver()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: RegisterFragmentArgs by navArgs()
        registerViewModel.firebaseToken.value = args.token
        Logger.t("Register").i(registerViewModel.firebaseToken.value.toString())
        val now = Calendar.getInstance()
        val mYear = now.get(Calendar.YEAR)
        val mMonth = now.get(Calendar.MONTH)
        val mDay = now.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            registerViewModel.birth.value = String.format("%d년 %d월 %d일", year, month+1, dayOfMonth)
            if(!registerViewModel.isBirthCheck.value!!){
                registerViewModel.isBirthCheck.value = true
            }
        }, mYear, mMonth, mDay)

        binding.edittextRegisterBirth.setOnClickListener {
            datePicker.show()
        }
        binding.buttonRegisterIdDuplication.setOnClickListener {
            registerViewModel.idDuplicationCheck()
        }
        binding.buttonRegisterSignup.setOnClickListener {
            if(registerViewModel.requestSignUp()){
                // 가입에 성공하면 메인으로 갑니다.
                // 물론 로그인한 상태로 말입니다.
                findNavController().navigate(R.id.action_registerFragment_to_mainFragment)
            }
        }
    }

    private fun initObserver() {
        registerViewModel.isBirthCheck.observe(viewLifecycleOwner) {
            if(it) {
                binding.textinputlayoutRegisterId.visibility = View.VISIBLE
                binding.buttonRegisterIdDuplication.visibility = View.VISIBLE
                binding.textviewRegisterIdState.visibility = View.VISIBLE
                binding.textviewRegisterYourinfo.setText(R.string.register_text_your_id)
            }
        }
        registerViewModel.isIdCheck.observe(viewLifecycleOwner) {
            if(it) {
                binding.textinputlayoutRegisterPassword.visibility = View.VISIBLE
                binding.textinputlayoutRegisterPasswordCheck.visibility = View.VISIBLE
                binding.textviewRegisterPasswordState.visibility = View.VISIBLE
                binding.textviewRegisterPasswordCheckState.visibility = View.VISIBLE
                binding.textviewRegisterYourinfo.setText(R.string.register_text_your_password)
            }
        }
        registerViewModel.isPasswordCheck.observe(viewLifecycleOwner) {
            binding.buttonRegisterSignup.visibility = if(it) View.VISIBLE else View.INVISIBLE
        }
    }
}
