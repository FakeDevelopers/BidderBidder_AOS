package com.fakedevelopers.bidderbidder.ui.register.acceptTerms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fakedevelopers.bidderbidder.HiltApplication
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentAcceptTermsBinding
import com.fakedevelopers.bidderbidder.databinding.IncludeTermCheckboxBinding
import com.fakedevelopers.bidderbidder.ui.register.RegistrationProgressState.ACCEPT_TERMS_CONTENTS
import com.fakedevelopers.bidderbidder.ui.register.UserRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AcceptTermsFragment : Fragment() {

    private var _binding: FragmentAcceptTermsBinding? = null

    private val binding get() = _binding!!
    private val acceptTermViewModel: AcceptTermsViewModel by viewModels()
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
        (requireContext().applicationContext as? HiltApplication)?.let {
            binding.includeAcceptTermsTitle.textView4.text = it.setPartialTextColor(
                getString(R.string.accept_terms_title),
                R.color.bidderbidder_primary,
                0,
                4
            )
            binding.includeAcceptTermsTitle.textView4.text = it.setPartialTextColor(
                binding.includeAcceptTermsTitle.textView4.text,
                R.color.bidderbidder_primary,
                19,
                21
            )
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.acceptAllState.collectLatest {
                    binding.checkboxAcceptTermsAcceptAll.isChecked = it
                    setAllTermsState(it)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                acceptTermViewModel.termListEvent.collectLatest {
                    if (it.isSuccessful) {
                        it.body()?.let { termList ->
                            setTermView(termList)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                acceptTermViewModel.termContentsEvent.collectLatest {
                    if (it.isSuccessful) {
                        it.body()?.let { _ ->
                            viewModel.acceptTermDetail = it.body().toString()
                            viewModel.setCurrentStep(ACCEPT_TERMS_CONTENTS)
                        }
                    }
                }
            }
        }
    }

    private fun setAllTermsState(state: Boolean) {
        binding.run {
            linearLayout4.children
                .map { it as ConstraintLayout }
                .map { it.children.first { view -> view is CheckBox } }
                .map { it as CheckBox }
                .forEach { it.isChecked = state }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setTermView(termList: TermListDto) {
        binding.linearLayout4.removeAllViews()
        termList.run {
            viewModel.setTermSize(required.size, optional.size)

            required.forEachIndexed { index, term ->
                addTermView(term, REQUIRED_TERM, index)
            }
            optional.forEachIndexed { index, term ->
                addTermView(term, OPTIONAL_TERM, index)
            }
        }
    }

    private fun addTermView(term: TermItemDto, type: Int, idx: Int) {
        binding.linearLayout4.addView(
            IncludeTermCheckboxBinding.inflate(
                layoutInflater,
                binding.linearLayout4,
                false
            ).apply {
                termTitle = term.name
                termId = term.id
                termIdx = idx
                termType = type
                vm = viewModel
                buttonReadMore.setOnClickListener {
                    acceptTermViewModel.requestTermContents(termId)
                }
            }.root
        )
    }

    companion object {
        const val OPTIONAL_TERM = 1
        const val REQUIRED_TERM = 0
    }
}
