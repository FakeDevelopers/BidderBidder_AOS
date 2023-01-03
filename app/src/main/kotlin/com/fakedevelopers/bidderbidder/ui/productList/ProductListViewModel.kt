package com.fakedevelopers.bidderbidder.ui.productList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.util.DateUtil
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import com.minseonglove.domain.usecase.GetProductListUseCase
import com.minseonglove.domain.usecase.IsLoadingAvailableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    dateUtil: DateUtil,
    args: SavedStateHandle,
    private val getProductListUseCase: GetProductListUseCase,
    private val isLoadingAvailableUseCase: IsLoadingAvailableUseCase
) : ViewModel() {

    private val _toProductDetail = MutableEventFlow<Long>()
    val toProductDetail = _toProductDetail.asEventFlow()

    private val _isRefreshing = MutableEventFlow<Boolean>()
    val isRefreshing = _isRefreshing.asEventFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _searchWord = MutableStateFlow("")
    val searchWord: StateFlow<String> get() = _searchWord

    private val _isEmptyResult = MutableStateFlow(false)
    val isEmptyResult: StateFlow<Boolean> get() = _isEmptyResult

    var isInitialize = true
        private set

    val adapter = ProductListAdapter(
        dateUtil = dateUtil,
        clickLoadMore = { getNextProductList() },
        showProductDetail = { productId -> showProductDetail(productId) }
    )

    init {
        viewModelScope.launch {
            _searchWord.emit(args.get<String>("searchWord") ?: "")
        }
        requestProductList(true)
    }

    fun getNextProductList() {
        // 이미 로딩 중일 때
        // 더보기가 활성화 중일 때
        // 마지막 상품까지 불러왔을 때
        if (isLoadingAvailableUseCase()) {
            return
        }

        requestProductList(false)
    }

    fun setInitializeState(state: Boolean) {
        isInitialize = state
    }

    fun requestProductList(isInitialize: Boolean) {
        this.isInitialize = isInitialize
        viewModelScope.launch {
            setLoadingState(true, isInitialize)
            val productItems = getProductListUseCase(searchWord.value, 0, isInitialize)
            adapter.submitList(productItems)
            _isEmptyResult.emit(productItems.isEmpty())
            setLoadingState(false, isInitialize)
        }
    }

    private fun showProductDetail(productId: Long) {
        viewModelScope.launch {
            _toProductDetail.emit(productId)
        }
    }

    private suspend fun setLoadingState(state: Boolean, isInitialize: Boolean) {
        if (isInitialize) {
            _isRefreshing.emit(state)
        } else {
            _isLoading.emit(state)
        }
    }
}
