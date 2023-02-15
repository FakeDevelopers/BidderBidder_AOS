package com.fakedevelopers.presentation.ui.productSearch

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentProductSearchBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductSearchFragment : BaseFragment<FragmentProductSearchBinding>(
    R.layout.fragment_product_search
) {
    private val viewModel: ProductSearchViewModel by viewModels()
    private val args: ProductSearchFragmentArgs by navArgs()

    private val imm by lazy {
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
    }

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.searchWord.collectLatest {
                findNavController().apply {
                    getViewModelStoreOwner(R.id.nav_graph).viewModelStore.clear()
                    navigate(ProductSearchFragmentDirections.actionProductSearchFragmentToProductListFragment(it))
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.searchBar.collectLatest {
                if (it.isNotEmpty()) {
                    binding.recyclerProductSearchResult.visibility = View.VISIBLE
                    binding.layoutProductSearchBeforeSearch.visibility = View.INVISIBLE
                    // 0.7초동안 키보드 조작이 없을때만 api를 요청한다.
                    delay(WAIT_BEFORE_REQUEST)
                    viewModel.requestSearchResult()
                } else {
                    binding.recyclerProductSearchResult.visibility = View.INVISIBLE
                    binding.layoutProductSearchBeforeSearch.visibility = View.VISIBLE
                    viewModel.clearResult()
                }
            }
        }
    }

    override fun initListener() {
        viewModel.setSearchBar(args.searchWord)
        binding.edittextToolbarSearch.run {
            // 키보드 올리기 전에 포커싱을 줘야함
            requestFocus()
            imm.showSoftInput(this, 0)
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    viewModel.searchEvent(v.text.toString())
                }
                true
            }
        }
        binding.buttonToolbarBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    companion object {
        private const val WAIT_BEFORE_REQUEST = 700L
    }
}
