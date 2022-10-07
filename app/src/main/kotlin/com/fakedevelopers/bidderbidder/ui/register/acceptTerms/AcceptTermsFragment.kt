package com.fakedevelopers.bidderbidder.ui.register.acceptTerms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentAcceptTermsBinding
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AcceptTermsFragment : Fragment() {

    private var _binding: FragmentAcceptTermsBinding? = null

    private val binding get() = _binding!!
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_accept_terms,
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.acceptAllState.collectLatest {
                    setAllTermsState(it)
                }
            }
        }
    }

    private fun setAllTermsState(state: Boolean) {
        binding.run {
            listOf(includeTerm1, includeTerm2, includeTerm3, includeTerm4, includeTerm5).forEach {
                it.checkboxAccept.isChecked = state
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
