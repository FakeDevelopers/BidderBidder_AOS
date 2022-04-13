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

    private lateinit var datePicker: DatePickerDialog
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: RegisterFragmentArgs by navArgs()
        registerViewModel.firebaseToken.value = args.token
        Logger.t("Register").i(registerViewModel.firebaseToken.value.toString())

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            registerViewModel.birthFocusable.value = false
            val now = Calendar.getInstance()
            val mYear = now.get(Calendar.YEAR)
            val mMonth = now.get(Calendar.MONTH)
            val mDay = now.get(Calendar.DAY_OF_MONTH)
            datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                registerViewModel.birth.value = "${mYear}-${mMonth+1}-${mDay}"
                if(!registerViewModel.birthCheck.value!!){
                    registerViewModel.birthCheck.value = true
                }
            }, mYear, mMonth, mDay)
        } else {
            // 낮은 버전은 에딧 텍스트로 입력합니다.
            registerViewModel.birthFocusable.value = true
            binding.edittextRegisterBirth.setHint(R.string.register_hint_birth_low_version)
        }

        binding.edittextRegisterBirth.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                datePicker.show()
            }
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
        registerViewModel.birthCheck.observe(viewLifecycleOwner) {
            if(it) {
                binding.textinputlayoutRegisterId.visibility = View.VISIBLE
                binding.buttonRegisterIdDuplication.visibility = View.VISIBLE
                binding.textviewRegisterIdState.visibility = View.VISIBLE
                binding.textviewRegisterYourinfo.setText(R.string.register_text_your_id)
            }
        }
        registerViewModel.idCheck.observe(viewLifecycleOwner) {
            if(it) {
                binding.textinputlayoutRegisterPassword.visibility = View.VISIBLE
                binding.textinputlayoutRegisterPasswordCheck.visibility = View.VISIBLE
                binding.textviewRegisterPasswordState.visibility = View.VISIBLE
                binding.textviewRegisterPasswordCheckState.visibility = View.VISIBLE
                binding.textviewRegisterYourinfo.setText(R.string.register_text_your_password)
            }
        }
        registerViewModel.passwordCheck.observe(viewLifecycleOwner) {
            binding.buttonRegisterSignup.visibility = if(it) View.VISIBLE else View.INVISIBLE
        }
    }
}
