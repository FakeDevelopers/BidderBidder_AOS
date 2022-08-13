package com.fakedevelopers.bidderbidder.ui.register.birth

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentUserRegistrationBirthBinding
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

class UserRegistrationBirthFragment : Fragment() {

    private var _binding: FragmentUserRegistrationBirthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }
    private val datePicker by lazy {
        val now = Calendar.getInstance(Locale.getDefault())
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            now.set(year, month, dayOfMonth)
            DateFormat.getDateInstance(DateFormat.LONG).apply {
                timeZone = now.timeZone
                viewModel.setBirth(format(now.time))
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_registration_birth,
            container,
            false
        )
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        binding.edittextBirth.setOnClickListener {
            datePicker.show()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
