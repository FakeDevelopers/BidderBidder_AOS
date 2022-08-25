package com.fakedevelopers.bidderbidder.ui.product_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec
import com.fakedevelopers.bidderbidder.api.repository.ProductDetailRepository
import com.fakedevelopers.bidderbidder.ui.util.ExpirationTimerTask
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductDetailRepository
) : ViewModel() {

    private val _productDetailResponse = MutableEventFlow<Response<ProductDetailDto>>()
    private val _title = MutableStateFlow("")
    private val _contents = MutableStateFlow("")
    private val _hopePrice = MutableStateFlow("")
    private val _minimumBid = MutableStateFlow("")
    private val _tick = MutableStateFlow("")
    private val _remainTime = MutableStateFlow("")
    private val _bidderCount = MutableStateFlow("")
    private val _bidInfoVisibility = MutableStateFlow(false)
    private lateinit var timerTask: ExpirationTimerTask

    val productDetailResponse = _productDetailResponse.asEventFlow()
    val productDetailAdapter = ProductDetailAdapter()
    val bidInfoAdapter = BidInfoAdapter()
    val title: StateFlow<String> get() = _title
    val contents: StateFlow<String> get() = _contents
    val hopePrice: StateFlow<String> get() = _hopePrice
    val minimumBid: StateFlow<String> get() = _minimumBid
    val tick: StateFlow<String> get() = _tick
    val remainTime: StateFlow<String> get() = _remainTime
    val bidderCount: StateFlow<String> get() = _bidderCount
    val bidInfoVisibility: StateFlow<Boolean> get() = _bidInfoVisibility

    fun productDetailRequest(productId: Long) {
        viewModelScope.launch {
            _productDetailResponse.emit(repository.getProductDetail(productId))
        }
    }

    fun initProductDetail(productDetailDto: ProductDetailDto?) {
        if (productDetailDto == null) {
            return
        }
        viewModelScope.launch {
            productDetailDto.run {
                _title.emit(productTitle)
                _contents.emit(productContent)
                _hopePrice.emit(makeWon(hopePrice))
                _minimumBid.emit(makeWon(openingBid))
                _tick.emit(makeWon(tick))
                _bidderCount.emit("${bidderCount}명")
                productDetailAdapter.submitList(images)
                bidInfoAdapter.submitList(bids)
                startTimerTask(expirationDate)
            }
        }
    }

    fun setBidInfoVisibility(state: Boolean) {
        // 아직 입찰 정보를 받아오지 않았다면 입찰 순위를 띄우지 않는다
        if (state && bidInfoAdapter.currentList.isEmpty()) {
            return
        }
        viewModelScope.launch {
            _bidInfoVisibility.emit(state)
        }
    }

    private fun startTimerTask(expirationDate: String) {
        // 의외로 화면을 나가면 알아서 종료 됩니다.
        timerTask = ExpirationTimerTask(
            expirationDate,
            1000,
            tick = { remainTimeString ->
                viewModelScope.launch {
                    _remainTime.emit(remainTimeString)
                }
            },
            finish = {
                viewModelScope.launch {
                    _remainTime.emit("마감")
                }
            }
        )
        timerTask.start()
    }

    private fun makeWon(price: Long) = "${dec.format(price)}원"
}
