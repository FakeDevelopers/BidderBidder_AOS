package com.fakedevelopers.presentation.ui.productList

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentProductListBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductListFragment : BaseFragment<FragmentProductListBinding>(
    R.layout.fragment_product_list
) {

    private val viewModel: ProductListViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    private val args: ProductListFragmentArgs by navArgs()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        viewModel.initSearchWord(args.searchWord)
    }

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.isRefreshing.collectLatest { state ->
                binding.swipeProductList.isRefreshing = state
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.toProductDetail.collectLatest { productId ->
                findNavController().navigate(
                    ProductListFragmentDirections.actionProductListFragmentToProductDetailFragment(productId)
                )
            }
        }
    }

    override fun initListener() {
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
        binding.recyclerProductList.run {
            layoutManager = null
            removeOnScrollListener(infinityScroll)
        }
        super.onDestroyView()
    }

    companion object {
        private const val REFRESH_COUNT = 5
    }
}
