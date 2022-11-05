package com.fakedevelopers.bidderbidder.ui.productList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductListRepository
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductListRepository
) : ViewModel() {

    private val productItems = mutableListOf<ProductListType>()

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

    private var isReadMoreVisible = true
    private var isLastProduct = false
    private var startNumber = -1L

    val adapter = ProductListAdapter(
        clickLoadMore = { clickLoadMore() },
        showProductDetail = { productId -> showProductDetail(productId) }
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
        if (isInitialize) {
            isLastProduct = false
            startNumber = LATEST_PRODUCT_ID
            productItems.clear()
        }
        this.isInitialize = isInitialize
        viewModelScope.launch {
            setLoadingState(true, isInitialize)
            repository.getProductList(searchWord.value, 0, LIST_COUNT, startNumber).let {
                if (it.isSuccessful) {
                    val resultItems = it.body() ?: return@let
                    handleResultItems(resultItems)
                } else {
                    ApiErrorHandler.printErrorMessage(it.errorBody())
                }
            }
            setLoadingState(false, isInitialize)
        }
    }

    private fun handleResultItems(resultItems: List<ProductItem>) {
        productItems.addAll(resultItems)
        startNumber = resultItems.lastOrNull()?.productId ?: LATEST_PRODUCT_ID
        if (resultItems.size < LIST_COUNT) {
            isReadMoreVisible = false
            isLastProduct = true
        }
        if (isReadMoreVisible) {
            productItems.add(ProductReadMore)
        }
        adapter.submitList(productItems.toList())
        viewModelScope.launch {
            _isEmptyResult.emit(resultItems.isEmpty())
        }
    }

    private fun clickLoadMore() {
        isReadMoreVisible = false
        productItems.remove(ProductReadMore)
        getNextProductList()
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

    companion object {
        private const val LATEST_PRODUCT_ID = -1L
        private const val LIST_COUNT = 20
    }
}
