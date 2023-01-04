package com.fakedevelopers.bidderbidder.ui.register.acceptTermDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentAcceptTermDetailBinding
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptTermDetailFragment : Fragment() {
    private var _binding: FragmentAcceptTermDetailBinding? = null

    private val binding get() = _binding!!
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_accept_term_detail,
            container,
            false
        )

        return binding.run {
            contents = viewModel.acceptTermDetail
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
