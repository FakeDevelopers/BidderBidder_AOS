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

    private val _eventFlow = MutableEventFlow<Event>()
    val eventFlow = _eventFlow.asEventFlow()

    private val _biddingButtonVisibility = MutableStateFlow(true)

    private lateinit var timerTask: ExpirationTimerTask

    val bidInfoAdapter = BidInfoAdapter()

    private var productId = -1L

    fun productDetailRequest(productId: Long) {
        this.productId = productId
        viewModelScope.launch {
            val result = repository.getProductDetail(productId)
            if (result.isSuccessful) {
                val detail = result.body() ?: ProductDetailDto()
                _productDetailDto.emit(detail)
                if (detail.images.isEmpty()) {
                    event(Event.ProductImages(listOf("")))
                } else {
                    event(Event.ProductImages(detail.images))
                }
                event(Event.CreatedDate(detail.createdDate))
                startTimerTask(detail.expirationDate)
                bidInfoAdapter.submitList(detail.bids)
            } else {
                ApiErrorHandler.printErrorMessage(result.errorBody())
            }
        }
    }

    private fun startTimerTask(expirationDate: String) {
        timerTask = ExpirationTimerTask(
            remainTime = dateUtil.getRemainTimeMillisecond(expirationDate) ?: 0L,
            tick = { remainTime ->
                viewModelScope.launch {
                    event(Event.Timer(remainTime))
                }
            },
            finish = {
                viewModelScope.launch {
                    event(Event.Expired(true))
                    _biddingButtonVisibility.emit(false)
                }
            }
        )
        timerTask.start()
    }

    private fun event(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    sealed class Event {
        data class ProductImages(val images: List<String>) : Event()
        data class Expired(val state: Boolean) : Event()
        data class Timer(val remainTime: RemainTime) : Event()
        data class CreatedDate(val date: String) : Event()
    }
}
