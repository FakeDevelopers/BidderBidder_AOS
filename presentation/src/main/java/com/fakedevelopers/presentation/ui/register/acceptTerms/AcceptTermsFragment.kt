package com.fakedevelopers.presentation.ui.register.acceptTerms

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.domain.model.TermItemDto
import com.fakedevelopers.domain.model.TermListDto
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAcceptTermsBinding
import com.fakedevelopers.presentation.databinding.IncludeTermCheckboxBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.register.RegistrationProgressState.ACCEPT_TERMS_CONTENTS
import com.fakedevelopers.presentation.ui.register.UserRegistrationViewModel
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AcceptTermsFragment : BaseFragment<FragmentAcceptTermsBinding>(
    R.layout.fragment_accept_terms
) {
    private val acceptTermViewModel: AcceptTermsViewModel by viewModels()
    private val viewModel: UserRegistrationViewModel by lazy {
        ViewModelProvider(requireActivity())[UserRegistrationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.includeAcceptTermsTitle.textView4.text =
            SpannableStringBuilder(getString(R.string.accept_terms_title)).apply {
                setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.bidderbidder_primary)),
                    0,
                    4,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.bidderbidder_primary)),
                    19,
                    21,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
    }

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.acceptAllState.collectLatest {
                binding.checkboxAcceptTermsAcceptAll.isChecked = it
                setAllTermsState(it)
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            acceptTermViewModel.termListEvent.collectLatest { result ->
                if (result.isSuccess) {
                    result.getOrNull()?.let { termList ->
                        setTermView(termList)
                    }
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            acceptTermViewModel.termContentsEvent.collectLatest { result ->
                result.isSuccess.let {
                    result.getOrNull()?.let { contents ->
                        viewModel.acceptTermDetail = contents
                        viewModel.setCurrentStep(ACCEPT_TERMS_CONTENTS)
                    }
                }
            }
        }
    }

    private fun setAllTermsState(state: Boolean) {
        binding.run {
            acceptTermList.children
                .forEach {
                    val constraintLayout = it as? ConstraintLayout ?: return@forEach
                    val checkBox: CheckBox = constraintLayout.children.first { view ->
                        view is CheckBox
                    } as? CheckBox ?: return@forEach

                    checkBox.isChecked = state
                }
        }
    }

    private fun setTermView(termList: TermListDto) {
        binding.acceptTermList.removeAllViews()
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
        binding.acceptTermList.addView(
            IncludeTermCheckboxBinding.inflate(
                layoutInflater,
                binding.acceptTermList,
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
