package com.fakedevelopers.bidderbidder.ui.productDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec
import com.fakedevelopers.bidderbidder.api.repository.ProductDetailRepository
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import com.fakedevelopers.bidderbidder.ui.util.ExpirationTimerTask
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductDetailRepository
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
    private val _confirmedBid = MutableStateFlow("")
    private val _confirmDialogText = MutableStateFlow("")
    private val _bidInfoVisibility = MutableStateFlow(false)
    private val _biddingVisibility = MutableStateFlow(false)
    private val _biddingEnabled = MutableStateFlow(true)
    private val _biddingButtonVisibility = MutableStateFlow(true)
    private val _confirmedBidVisibility = MutableStateFlow(true)
    private val _confirmDialogVisibility = MutableStateFlow(false)

    // 즉시 구매가 고정 이벤트
    private val _ceilPriceEvent = MutableEventFlow<String>()
    private val _moveOneTickEvent = MutableEventFlow<Long>()
    private val _sendMessageEvent = MutableEventFlow<String>()
    private lateinit var timerTask: ExpirationTimerTask

    // 입찰가 입력에 사용할 호가값
    private var tickValue = 0

    // 현재 입찰가 입력
    private var currentBid = ""

    // 요청 유저 ID
    private var requestUserId = 0L

    // 요청 입찰가
    private var requestBid = 0L

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
    val confirmedBid: StateFlow<String> get() = _confirmedBid
    val confirmDialogText: StateFlow<String> get() = _confirmDialogText
    val bidInfoVisibility: StateFlow<Boolean> get() = _bidInfoVisibility
    val biddingVisibility: StateFlow<Boolean> get() = _biddingVisibility
    val biddingEnabled: StateFlow<Boolean> get() = _biddingEnabled
    val biddingButtonVisibility: StateFlow<Boolean> get() = _biddingButtonVisibility
    val confirmedBidVisibility: StateFlow<Boolean> get() = _confirmedBidVisibility
    val confirmDialogVisibility: StateFlow<Boolean> get() = _confirmDialogVisibility
    val ceilPriceEvent = _ceilPriceEvent.asEventFlow()
    val moveOneTickEvent = _moveOneTickEvent.asEventFlow()
    val sendMessageEvent = _sendMessageEvent.asEventFlow()

    // 입찰가 검증에 사용할 희망가, 입찰가
    var hopePriceValue = -1L
        private set
    var minimumBidValue = -1L
        private set

    // 상품 id
    var productId = -1L
        private set

    fun productDetailRequest(productId: Long) {
        this.productId = productId
        viewModelScope.launch {
            repository.getProductDetail(productId).let {
                if (it.isSuccessful) {
                    _productDetailResponse.emit(it)
                } else {
                    ApiErrorHandler.handleError(it.errorBody())
                }
            }
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
                minimumBidValue = getMinimumBid(openingBid, tick, bids)
                _minimumBid.emit(makeWon(minimumBidValue))
                _tick.emit(makeWon(tick.toLong()))
                tickValue = tick
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
                // 입찰 정보 화면은 끈다
                _bidInfoVisibility.emit(false)
            }
            return
        }
        // 최종 확인 다이얼로그가 올라와 있으면 무시
        if (confirmDialogVisibility.value) {
            return
        }
        // 입찰 조건이 안맞으면 무시
        if (!checkBiddingCondition()) {
            return
        }
        // 입찰 조건 검사
        viewModelScope.launch {
            // 최종 확인 다이얼로그 표시
            _confirmDialogText.emit("${confirmedBid.value}에 입찰하시겠습니까?")
            _confirmDialogVisibility.emit(true)
            // 입찰가 조작은 비활성화
            _biddingEnabled.emit(false)
            // 입찰 정보 화면은 끈다
            _bidInfoVisibility.emit(false)
        }
    }

    // 입찰 다이얼로그 닫기
    fun closeDialog() {
        viewModelScope.launch {
            _confirmDialogVisibility.emit(false)
            _biddingVisibility.emit(false)
            _bidInfoVisibility.emit(false)
        }
        setBiddingEnabled(true)
    }

    // 입찰 하기
    fun startBidding() {
        // 다이얼로그를 닫고
        closeDialog()
        // api 요청
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
        // 최종 입찰가 갱신
        setConfirmedBid(bid)
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

    fun setBiddingEnabled(state: Boolean) {
        viewModelScope.launch {
            _biddingEnabled.emit(state)
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

    private fun setConfirmedBid(bid: String) {
        val bidValue = bid.replace(",", "").toLongOrDefault(0)
        var floorBid = bidValue - (bidValue % tickValue)
        if (hopePriceValue != -1L && bidValue > hopePriceValue) {
            floorBid = hopePriceValue
            // 즉시 구매가 이상이라면 즉시 구매가로 고정시킨다.
            viewModelScope.launch {
                _ceilPriceEvent.emit(hopePriceValue.toString())
            }
        }
        viewModelScope.launch {
            // 최종 입찰가
            _confirmedBid.emit(makeWon(floorBid))
            // 최종 입찰가 < 최소 입찰가 라면 invisible
            _confirmedBidVisibility.emit(floorBid >= minimumBidValue)
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

    private fun getMinimumBid(openingBid: Long, tick: Int, bids: List<BidInfo>) =
        when {
            bids.isEmpty() -> openingBid
            bids.size in 1..3 -> openingBid + tick
            else -> bids[3].bid + tick
        }

    private fun productBidRequest() {
        // 입찰가 조작을 막고 api 요청
        setBiddingEnabled(false)
        viewModelScope.launch {
            repository.postProductBid(productId, requestUserId, requestBid).let {
                if (it.isSuccessful) {
                    _productBidResponse.emit(it)
                } else {
                    ApiErrorHandler.handleError(it.errorBody())
                }
            }
        }
    }

    // 입찰 조건 검사
    private fun checkBiddingCondition(): Boolean {
        val id = userId.value.toLongOrNull()
        val bid = confirmedBid.value.replace("[^\\d]".toRegex(), "").toLongOrNull()
        if (id == null) {
            sendMessage("유저 ID가 올바르지 않아!")
            return false
        } else if (bid == null || bid < minimumBidValue) {
            sendMessage("입찰가가 올바르지 않아!")
            return false
        }
        // 올바른 값이라면 요청 변수에 담는다
        requestUserId = id
        requestBid = bid
        return true
    }

    private fun sendMessage(msg: String) {
        viewModelScope.launch {
            _sendMessageEvent.emit(msg)
        }
    }

    private fun makeWon(price: Long) = "${dec.format(price)}원"
}
