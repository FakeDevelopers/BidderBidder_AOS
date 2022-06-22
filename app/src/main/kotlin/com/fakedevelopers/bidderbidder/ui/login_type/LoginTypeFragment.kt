package com.fakedevelopers.bidderbidder.ui.login_type

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginTypeBinding

class LoginTypeFragment : Fragment() {

    private var _binding: FragmentLoginTypeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login_type,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sp = SpannableStringBuilder(getString(R.string.logintype_welcometext)).apply {
            setSpan(
                ForegroundColorSpan(requireActivity().getColor(R.color.bidderbidder_primary)),
                0,
                4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.textviewLogintypeWelcometext.text = sp
        binding.buttonLogintypeCommon.layoutLoginType.setOnClickListener {
            findNavController().navigate(R.id.action_loginTypeFragment_to_loginFragment)
        }
        binding.textViewLogintypeRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_loginTypeFragment_to_phoneAuthFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
