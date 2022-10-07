package com.fakedevelopers.bidderbidder.ui.productList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductListRepository
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductListRepository
) : ViewModel() {

    private val productItems = mutableListOf<ProductItem>()

    private val _toProductDetail = MutableEventFlow<Long>()
    val toProductDetail = _toProductDetail.asEventFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _searchWord = MutableStateFlow("")
    val searchWord: StateFlow<String> get() = _searchWord

    private val _isEmptyResult = MutableStateFlow(false)
    val isEmptyResult: StateFlow<Boolean> get() = _isEmptyResult

    var isInitialize = true
        private set

    private var isReadMoreVisible = true
    private var isLastProduct = false
    private var startNumber = -1L

    val adapter = ProductListAdapter(
        clickLoadMore = { clickLoadMore() },
        clickProductDetail = { productId -> clickProductDetail(productId) },
        isReadMoreVisible = { isReadMoreVisible }
    )

    fun getNextProductList() {
        // 이미 로딩 중일 때
        // 더보기가 활성화 중일 때
        // 마지막 상품까지 불러왔을 때
        if (isLoading.value || isReadMoreVisible || isLastProduct) {
            return
        }

        requestProductList(false)
    }

    fun setInitializeState(state: Boolean) {
        isInitialize = state
    }

    fun setSearchWord(word: String) {
        viewModelScope.launch {
            _searchWord.emit(word)
        }
    }

    fun requestProductList(isInitialize: Boolean) {
        // 최초 실행이거나 리프레쉬 중이면 startNumber를 초기화 한다.
        if (isInitialize) {
            isLastProduct = false
            startNumber = -1
            productItems.clear()
        }
        this.isInitialize = isInitialize
        viewModelScope.launch {
            // 추가하기 전에 로딩 띄우기
            setLoadingState(true)
            repository.getProductList(searchWord.value, 0, LIST_COUNT, startNumber).let {
                if (it.isSuccessful) {
                    val resultItems = it.body() ?: return@let
                    productItems.addAll(resultItems)
                    if (productItems.isNotEmpty()) {
                        startNumber = productItems.last().productId
                    }
                    adapter.submitList(productItems.toList())
                    // 요청한 것 보다 더 적게 받아오면 끝자락이라고 판단
                    if (resultItems.size < LIST_COUNT) {
                        isReadMoreVisible = false
                        isLastProduct = true
                    }
                    _isEmptyResult.emit(resultItems.isEmpty())
                } else {
                    Logger.e(it.errorBody().toString())
                }
            }
            setLoadingState(false)
        }
    }

    private fun clickLoadMore() {
        isReadMoreVisible = false
        getNextProductList()
    }

    private fun clickProductDetail(productId: Long) {
        viewModelScope.launch {
            _toProductDetail.emit(productId)
        }
    }

    private fun setLoadingState(state: Boolean) {
        viewModelScope.launch {
            _isLoading.emit(state)
        }
    }

    companion object {
        const val LIST_COUNT = 20
    }
}
