package com.fakedevelopers.bidderbidder.ui.productList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentProductListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductListViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }
    private val infinityScroll by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (binding.recyclerProductList.layoutManager as? LinearLayoutManager)?.let {
                    val lastVisibleItem = it.findLastCompletelyVisibleItemPosition()
                    if (it.itemCount <= lastVisibleItem + REFRESH_COUNT) {
                        viewModel.getNextProductList()
                    }
                }
            }
        }
    }
    private val linearLayoutManager by lazy {
        object : LinearLayoutManager(requireContext()) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                if (viewModel.isInitialize) {
                    binding.recyclerProductList.post {
                        binding.recyclerProductList.layoutManager?.scrollToPosition(0)
                    }
                    viewModel.setInitializeState(false)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_product_list,
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
        val args: ProductListFragmentArgs by navArgs()
        if (viewModel.isInitialize) {
            viewModel.setSearchWord(args.searchWord)
            viewModel.requestProductList(true)
        }
        initListener()
        initCollector()
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isRefreshing.collectLatest { state ->
                    binding.swipeProductList.isRefreshing = state
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.toProductDetail.collectLatest { productId ->
                    findNavController().navigate(
                        ProductListFragmentDirections.actionProductListFragmentToProductDetailFragment(productId)
                    )
                }
            }
        }
    }

    private fun initListener() {
        binding.swipeProductList.setOnRefreshListener {
            viewModel.requestProductList(true)
        }
        binding.recyclerProductList.apply {
            addOnScrollListener(infinityScroll)
            layoutManager = linearLayoutManager
        }
        binding.toolbarProductList.setOnMenuItemClickListener {
            if (it.itemId == R.id.toolbar_search) {
                findNavController().navigate(
                    ProductListFragmentDirections
                        .actionProductListFragmentToProductSearchFragment(viewModel.searchWord.value)
                )
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerProductList.run {
            layoutManager = null
            removeOnScrollListener(infinityScroll)
        }
        _binding = null
    }

    companion object {
        private const val REFRESH_COUNT = 5
    }
}
