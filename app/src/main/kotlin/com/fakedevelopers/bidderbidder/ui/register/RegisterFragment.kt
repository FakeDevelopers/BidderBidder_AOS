package com.fakedevelopers.bidderbidder.ui.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentRegisterBinding
import com.fakedevelopers.bidderbidder.ui.register.RegisterViewModel.RegisterEvent
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

class RegisterFragment : Fragment() {

    private lateinit var datePicker: DatePickerDialog

    private val binding: FragmentRegisterBinding by viewBinding(createMethod = CreateMethod.INFLATE)
    private val viewModel: RegisterViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AnimationUtils.loadAnimation(requireContext(), R.anim.animation_shake).let {
                    binding.constraintlayoutRegister.startAnimation(it)
                }
                showToast("못 돌아간다!")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.addLogAdapter(AndroidLogAdapter())
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: RegisterFragmentArgs by navArgs()
        viewModel.firebaseToken.value = args.token
        Logger.t("Register").i(viewModel.firebaseToken.value)
        initListener()
        initCollector()
    }

    private fun initListener() {
        val now = Calendar.getInstance(Locale.getDefault())
        val mYear = now.get(Calendar.YEAR)
        val mMonth = now.get(Calendar.MONTH)
        val mDay = now.get(Calendar.DAY_OF_MONTH)
        datePicker = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            now.set(year, month, dayOfMonth)
            DateFormat.getDateInstance(DateFormat.LONG).apply {
                timeZone = now.timeZone
                viewModel.setBirth(format(now.time))
            }
        }, mYear, mMonth, mDay)

        binding.edittextRegisterBirth.setOnClickListener {
            datePicker.show()
        }

        binding.buttonRegisterSignup.setOnClickListener {
            if (viewModel.requestSignUp()) {
                // 가입에 성공하면 메인으로 갑니다.
                // 물론 로그인한 상태로 말입니다.
                findNavController().navigate(R.id.action_registerFragment_to_mainFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            backPressedCallback
        )
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collectLatest { event -> handleRegisterEvent(event) }
            }
        }
    }

    private fun handleRegisterEvent(event: RegisterEvent) {
        with(binding) {
            when (event) {
                is RegisterEvent.BirthCheck -> {
                    if (event.check) {
                        textinputlayoutRegisterId.visibility = View.VISIBLE
                        buttonRegisterIdDuplication.visibility = View.VISIBLE
                        textviewRegisterIdState.visibility = View.VISIBLE
                        textviewRegisterYourinfo.setText(R.string.register_text_your_id)
                    }
                }
                is RegisterEvent.IdCheck -> {
                    if (event.check) {
                        textinputlayoutRegisterPassword.visibility = View.VISIBLE
                        textinputlayoutRegisterPasswordCheck.visibility = View.VISIBLE
                        textviewRegisterPasswordState.visibility = View.VISIBLE
                        textviewRegisterPasswordCheckState.visibility = View.VISIBLE
                        textviewRegisterYourinfo.setText(R.string.register_text_your_password)
                    }
                }
                is RegisterEvent.PasswordCheck -> {
                    buttonRegisterSignup.visibility = if (event.check) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
