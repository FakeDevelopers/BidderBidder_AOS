package com.fakedevelopers.bidderbidder.ui.productSearch

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.datastore.DatastoreSetting.Companion.SEARCH_HISTORY
import com.fakedevelopers.bidderbidder.api.datastore.DatastoreSetting.Companion.datastore
import com.fakedevelopers.bidderbidder.databinding.FragmentProductSearchBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException

class ProductSearchFragment : Fragment() {

    private var _binding: FragmentProductSearchBinding? = null

    private val viewModel: ProductSearchViewModel by viewModels()
    private val binding get() = _binding!!
    private val args: ProductSearchFragmentArgs by navArgs()
    private val imm by lazy {
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    // datastore
    private val searchHistory by lazy {
        requireContext().datastore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[SEARCH_HISTORY]?.toList() ?: listOf()
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_product_search,
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
        initCollector()
        initListener()
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchHistory.collect {
                    viewModel.setHistoryList(it)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchWord.collectLatest {
                    findNavController().apply {
                        getViewModelStoreOwner(R.id.nav_graph).viewModelStore.clear()
                        navigate(ProductSearchFragmentDirections.actionProductSearchFragmentToProductListFragment(it))
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historySet.collect {
                    requireContext().datastore.edit { preferences ->
                        preferences[SEARCH_HISTORY] = it
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
    }

    private fun initListener() {
        binding.toolbarProductSearch.apply {
            edittextToolbarSearch.let {
                // 키보드 올리기 전에 포커싱을 줘야함
                it.requestFocus()
                imm.showSoftInput(it, 0)
                viewModel.setSearchBar(args.searchWord)
                it.setOnEditorActionListener { v, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        imm.hideSoftInputFromWindow(it.windowToken, 0)
                        viewModel.searchEvent(v.text.toString())
                    }
                    true
                }
            }
            buttonToolbarBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        }
        binding.textviewProductSearchEraseAll.setOnClickListener {
            viewModel.clearHistory()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val WAIT_BEFORE_REQUEST = 700L
    }
}
