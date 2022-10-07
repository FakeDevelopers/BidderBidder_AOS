package com.fakedevelopers.bidderbidder.ui.productDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentProductDetailBinding
import com.fakedevelopers.bidderbidder.ui.productRegistration.PriceTextWatcher
import com.fakedevelopers.bidderbidder.ui.productRegistration.PriceTextWatcher.Companion.MAX_PRICE_LENGTH
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null

    private val viewModel: ProductDetailViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_product_detail,
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
        val args: ProductDetailFragmentArgs by navArgs()
        if (args.productId != -1L) {
            viewModel.productDetailRequest(args.productId)
        }
        initCollector()
        // 입찰가 입력 필터 등록
        PriceTextWatcher.addEditTextFilter(binding.includeProductDetailBidding.edittextBidPrice, MAX_PRICE_LENGTH) {
            binding.includeProductDetailBidding.edittextBidPrice.apply {
                viewModel.setCurrentBid(text.toString())
            }
        }
    }

    private fun initCollector() {
        // 상품 상세정보 api
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productDetailResponse.collectLatest {
                    if (it.isSuccessful) {
                        viewModel.initProductDetail(it.body())
                    } else {
                        ApiErrorHandler.print(it.errorBody())
                    }
                }
            }
        }
        // 입찰 api
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productBidResponse.collectLatest {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "입찰 성공", Toast.LENGTH_SHORT).show()
                        // 새로고침
                        findNavController().navigate(
                            ProductDetailFragmentDirections.actionProductDetailFragmentSelf(viewModel.productId)
                        )
                    } else {
                        // 실패했으면 입찰가 조작을 다시 활성화
                        viewModel.setBiddingEnabled(true)
                        ApiErrorHandler.print(it.errorBody())
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.biddingVisibility.collectLatest { state ->
                    setBiddingVisibility(state)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.moveOneTickEvent.collectLatest { tick ->
                    val currentBid = getCurrentBid()
                    val updatedBid = setValidBid(currentBid + tick - (currentBid % tick))
                    binding.includeProductDetailBidding.edittextBidPrice.setText(updatedBid.toString())
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sendMessageEvent.collectLatest { msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        // 입찰가 고정
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ceilPriceEvent.collectLatest { price ->
                    binding.includeProductDetailBidding.edittextBidPrice.setText(price)
                }
            }
        }
    }

    private fun setBiddingVisibility(state: Boolean) {
        // 등장하는 애니메이션이면 VISIBLE 한 다음 애니메이션 동작
        // 사라지는 애니메이션이면 INVISIBLE 하기 전에 애니메이션 동작
        binding.includeProductDetailBidding.root.apply {
            if (!state && visibility == View.VISIBLE) {
                startAnimation(getAnimation(R.anim.animation_translate_down))
            }
            visibility = if (state) {
                startAnimation(getAnimation(R.anim.animation_translate_up))
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

    private fun getAnimation(id: Int) =
        AnimationUtils.loadAnimation(requireContext(), id)

    private fun getCurrentBid() =
        binding.includeProductDetailBidding.edittextBidPrice.text.toString()
            .replace(",", "").toLongOrDefault(0)

    private fun setValidBid(bid: Long) =
        when {
            viewModel.hopePriceValue != -1L && viewModel.hopePriceValue < bid -> viewModel.hopePriceValue
            viewModel.minimumBidValue > bid -> viewModel.minimumBidValue
            else -> bid
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
