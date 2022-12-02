package com.fakedevelopers.bidderbidder.ui.productDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductDetailRepository
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
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
    private val repository: ProductDetailRepository
) : ViewModel() {

    private val _productDetailDto = MutableStateFlow(ProductDetailDto())
    val productDetailDto: StateFlow<ProductDetailDto> get() = _productDetailDto

    private val _productPictures = MutableStateFlow<List<String>>(emptyList())
    val productPictures: StateFlow<List<String>> get() = _productPictures

    private val _remainTimeState = MutableStateFlow("마감까지")
    private val _remainTime = MutableStateFlow("")

    private val _sendMessageEvent = MutableEventFlow<String>()
    val sendMessageEvent = _sendMessageEvent.asEventFlow()

    private val _biddingButtonVisibility = MutableStateFlow(true)

    private lateinit var timerTask: ExpirationTimerTask

    val bidInfoAdapter = BidInfoAdapter()
    val remainTimeState: StateFlow<String> get() = _remainTimeState
    val remainTime: StateFlow<String> get() = _remainTime

    // 상품 id
    private var productId = -1L

    fun productDetailRequest(productId: Long) {
        this.productId = productId
        viewModelScope.launch {
            repository.getProductDetail(productId).let {
                if (it.isSuccessful) {
                    val detail = it.body() ?: ProductDetailDto()
                    _productDetailDto.emit(detail)
                    _productPictures.emit(detail.images)
                    bidInfoAdapter.submitList(detail.bids)
                } else {
                    ApiErrorHandler.printErrorMessage(it.errorBody())
                }
            }
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
                    _remainTime.emit("")
                    _remainTimeState.emit("마감")
                    _biddingButtonVisibility.emit(false)
                }
            }
        )
        timerTask.start()
    }
}
