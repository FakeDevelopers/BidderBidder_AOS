package com.fakedevelopers.presentation.ui.productDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.ProductDetailInfo
import com.fakedevelopers.domain.usecase.GetProductDetailUseCase
import com.fakedevelopers.presentation.model.RemainTime
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import com.fakedevelopers.presentation.ui.util.DateUtil
import com.fakedevelopers.presentation.ui.util.ExpirationTimerTask
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.logging.helper.stringify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val dateUtil: DateUtil,
    private val getProductDetailUseCase: GetProductDetailUseCase
) : ViewModel() {

    private val _productDetailInfo = MutableStateFlow(ProductDetailInfo())
    val productDetailInfo: StateFlow<ProductDetailInfo> get() = _productDetailInfo

    private val _eventFlow = MutableEventFlow<Event>()
    val eventFlow = _eventFlow.asEventFlow()

    private val _biddingButtonVisibility = MutableStateFlow(true)

    private lateinit var timerTask: ExpirationTimerTask

    val bidInfoAdapter = BidInfoAdapter()

    var productId = -1L

    fun productDetailRequest(productId: Long) {
        this.productId = productId
        viewModelScope.launch {
            val result = getProductDetailUseCase(productId)
            if (result.isSuccess) {
                val detail = result.getOrDefault(ProductDetailInfo())
                _productDetailInfo.emit(detail)
                if (detail.images.isEmpty()) {
                    event(Event.ProductImages(listOf("")))
                } else {
                    event(Event.ProductImages(detail.images))
                }
                event(Event.CreatedDate(detail.createdDate))
                startTimerTask(detail.expirationDate)
                bidInfoAdapter.submitList(detail.bids)
            } else {
                ApiErrorHandler.printMessage(result.exceptionOrNull()?.stringify().toString())
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
