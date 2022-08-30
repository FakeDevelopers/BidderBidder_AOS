package com.fakedevelopers.bidderbidder.ui.product_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec
import com.fakedevelopers.bidderbidder.api.repository.ProductBidRepository
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
    private val productDetailRepository: ProductDetailRepository,
    private val productBidRepository: ProductBidRepository
) : ViewModel() {

    private val _productDetailResponse = MutableEventFlow<Response<ProductDetailDto>>()
    private val _productBidResponse = MutableEventFlow<Response<String>>()
    private val _title = MutableStateFlow("")
    private val _contents = MutableStateFlow("")
    private val _hopePrice = MutableStateFlow("")
    private val _minimumBid = MutableStateFlow("")
    private val _tick = MutableStateFlow("")
    private val _remainTimeState = MutableStateFlow("마감까지")
    private val _remainTime = MutableStateFlow("")
    private val _bidderCount = MutableStateFlow("")
    private val _bidInfoVisibility = MutableStateFlow(false)
    private val _biddingVisibility = MutableStateFlow(false)
    private val _biddingButtonVisibility = MutableStateFlow(true)
    private val _moveOneTickEvent = MutableEventFlow<Long>()
    private val _sendMessage = MutableEventFlow<String>()
    private lateinit var timerTask: ExpirationTimerTask
    // 입찰가 입력에 사용할 호가값
    private var tickValue = 0
    // 현재 입찰가 입력
    private var currentBid = ""
    // 상품 id
    private var productId = -1L

    val productDetailResponse = _productDetailResponse.asEventFlow()
    val productBidResponse = _productBidResponse.asEventFlow()
    val productDetailAdapter = ProductDetailAdapter()
    val bidInfoAdapter = BidInfoAdapter()
    val userId = MutableStateFlow("")
    val title: StateFlow<String> get() = _title
    val contents: StateFlow<String> get() = _contents
    val hopePrice: StateFlow<String> get() = _hopePrice
    val minimumBid: StateFlow<String> get() = _minimumBid
    val tick: StateFlow<String> get() = _tick
    val remainTimeState: StateFlow<String> get() = _remainTimeState
    val remainTime: StateFlow<String> get() = _remainTime
    val bidderCount: StateFlow<String> get() = _bidderCount
    val bidInfoVisibility: StateFlow<Boolean> get() = _bidInfoVisibility
    val biddingVisibility: StateFlow<Boolean> get() = _biddingVisibility
    val biddingButtonVisibility: StateFlow<Boolean> get() = _biddingButtonVisibility
    val moveOneTickEvent = _moveOneTickEvent.asEventFlow()
    val sendMessage = _sendMessage.asEventFlow()
    // 입찰가 검증에 사용할 희망가, 입찰가
    var hopePriceValue = -1L
        private set
    var minimumBidValue = -1L
        private set

    fun productDetailRequest(productId: Long) {
        this.productId = productId
        viewModelScope.launch {
            _productDetailResponse.emit(productDetailRepository.getProductDetail(productId))
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
                hopePriceValue = hopePrice
                _minimumBid.emit(makeWon(openingBid))
                minimumBidValue = openingBid
                _tick.emit(makeWon(tick))
                tickValue = tick.toInt()
                _bidderCount.emit("${bidderCount}명")
                _moveOneTickEvent.emit(openingBid)
                productDetailAdapter.submitList(images)
                bidInfoAdapter.submitList(bids)
                startTimerTask(expirationDate)
            }
        }
    }

    // 입찰 버튼 처리
    fun clickBidding() {
        // 입찰하기 뷰가 내려가 있다면 올려만 주고 끝낸다.
        if (!biddingVisibility.value) {
            viewModelScope.launch {
                _biddingVisibility.emit(true)
            }
            return
        }
        // 입찰 api 요청
        productBidRequest()
    }

    // 1틱 증감
    // true : 증가, false : 감소
    fun moveOneTick(state: Boolean) {
        val plusOrMinus = if (state) 1 else -1
        viewModelScope.launch {
            _moveOneTickEvent.emit((tickValue * plusOrMinus).toLong())
        }
    }

    fun setCurrentBid(bid: String) {
        currentBid = bid
    }

    fun setBiddingVisibility(state: Boolean) {
        // 아직 입찰 정보를 받아오지 않았다면 입찰 화면을 띄우지 않는다
        if (state && bidInfoAdapter.currentList.isEmpty()) {
            return
        }
        viewModelScope.launch {
            if (state) {
                _bidInfoVisibility.emit(false)
            }
            _biddingVisibility.emit(state)
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
                    _remainTime.emit("")
                    _remainTimeState.emit("마감")
                    _biddingVisibility.emit(false)
                    _biddingButtonVisibility.emit(false)
                }
            }
        )
        timerTask.start()
    }

    private fun productBidRequest() {
        val id = userId.value.toLongOrNull()
        val bid = currentBid.replace(",", "").toLongOrNull()
        if (id == null) {
            sendToastMessage("유저 ID가 올바르지 않아!")
            return
        } else if (bid == null) {
            sendToastMessage("입찰가가 올바르지 않아!")
            return
        }
        viewModelScope.launch {
            _productBidResponse.emit(productBidRepository.postProductBid(productId, id, bid))
        }
    }

    private fun sendToastMessage(msg: String) {
        viewModelScope.launch {
            _sendMessage.emit(msg)
        }
    }

    private fun makeWon(price: Long) = "${dec.format(price)}원"
}
