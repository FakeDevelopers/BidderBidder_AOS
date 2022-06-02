package com.fakedevelopers.bidderbidder.ui.product_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentProductListBinding
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductListFragment : Fragment() {

    private lateinit var productListAdapter: ProductListAdapter

    private var _binding: FragmentProductListBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ProductListViewModel by viewModels()

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
        initListener()
        viewModel.requestProductList(true)
        collectProductList()
    }

    private fun collectProductList() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productList.collectLatest {
                    updateRecyclerView(it)
                    binding.swipeProductList.isRefreshing = false
                }
            }
        }
    }

    private fun updateRecyclerView(productList: List<ProductListDto>) {
        if (!::productListAdapter.isInitialized) {
            productListAdapter = ProductListAdapter(onClick = {
                // 더보기 버튼이 애매하게 가려진 채로 누르면 크래쉬가 납니다.
                // 그래서 우선 더보기를 누르면 스크롤을 가장 끝까지 내린다음 클릭 이벤트를 수행 합니다.
                binding.recyclerProductList.scrollToPosition(productListAdapter.itemCount + 1)
                viewModel.clickReadMore()
            }) {
                getString(R.string.productlist_text_price, it)
            }.apply {
                submitList(productList)
            }
            val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider_product_list)!!)
            }
            binding.recyclerProductList.apply {
                addItemDecoration(divider)
                adapter = productListAdapter
            }
        } else {
            productListAdapter.submitList(productList.toList())
        }
    }

    private fun initListener() {
        binding.swipeProductList.setOnRefreshListener {
            viewModel.requestProductList(true)
        }
        binding.recyclerProductList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding.recyclerProductList.layoutManager.let {
                    val lastVisibleItem = (it as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    if (it.itemCount <= lastVisibleItem + REFRESH_COUNT) {
                        Logger.t("recycler").i("리스트 하나 더")
                        viewModel.getNextProductList()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REFRESH_COUNT = 5
    }
}
