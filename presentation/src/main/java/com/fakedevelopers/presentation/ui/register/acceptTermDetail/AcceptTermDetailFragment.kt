package com.fakedevelopers.presentation.ui.register.acceptTermDetail

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAcceptTermDetailBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.register.UserRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AcceptTermDetailFragment : BaseFragment<FragmentAcceptTermDetailBinding>(
    R.layout.fragment_accept_term_detail
) {
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.contents = viewModel.acceptTermDetail
    }
}
