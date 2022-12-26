package com.fakedevelopers.bidderbidder.ui.productDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductDetailRepository
import com.fakedevelopers.bidderbidder.model.RemainTime
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import com.fakedevelopers.bidderbidder.ui.util.DateUtil
import com.fakedevelopers.bidderbidder.ui.util.ExpirationTimerTask
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val dateUtil: DateUtil,
    private val repository: ProductDetailRepository
) : ViewModel() {

    private val _productDetailDto = MutableStateFlow(ProductDetailDto())
    val productDetailDto: StateFlow<ProductDetailDto> get() = _productDetailDto

    private val _productPictures = MutableStateFlow<List<String>>(emptyList())
    val productPictures: StateFlow<List<String>> get() = _productPictures

    private val _expiredEvent = MutableEventFlow<Boolean>()
    val expiredEvent = _expiredEvent.asEventFlow()

    private val _timerEvent = MutableEventFlow<RemainTime>()
    val timerEvent = _timerEvent.asEventFlow()

    private val _sendMessageEvent = MutableEventFlow<String>()
    val sendMessageEvent = _sendMessageEvent.asEventFlow()

    private val _biddingButtonVisibility = MutableStateFlow(true)

    private lateinit var timerTask: ExpirationTimerTask

    val bidInfoAdapter = BidInfoAdapter()

    private var productId = -1L

    fun productDetailRequest(productId: Long) {
        this.productId = productId
        viewModelScope.launch {
            repository.getProductDetail(productId).let {
                if (it.isSuccessful) {
                    val detail = it.body() ?: ProductDetailDto()
                    _productDetailDto.emit(detail)
                    _productPictures.emit(detail.images)
                    startTimerTask(detail.expirationDate)
                    bidInfoAdapter.submitList(detail.bids)
                } else {
                    ApiErrorHandler.printErrorMessage(it.errorBody())
                }
            }
        }
    }

    private fun startTimerTask(expirationDate: String) {
        timerTask = ExpirationTimerTask(
            remainTime = dateUtil.getRemainTimeMillisecond(expirationDate) ?: 0L,
            tick = { remainTime ->
                viewModelScope.launch {
                    _timerEvent.emit(remainTime)
                }
            },
            finish = {
                viewModelScope.launch {
                    _expiredEvent.emit(true)
                    _biddingButtonVisibility.emit(false)
                }
            }
        )
        timerTask.start()
    }
}
