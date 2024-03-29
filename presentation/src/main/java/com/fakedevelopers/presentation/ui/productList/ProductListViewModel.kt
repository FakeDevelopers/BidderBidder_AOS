package com.fakedevelopers.presentation.ui.productList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.ProductItem
import com.fakedevelopers.domain.model.ProductReadMore
import com.fakedevelopers.domain.usecase.GetProductListUseCase
import com.fakedevelopers.presentation.ui.util.DateUtil
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    dateUtil: DateUtil,
    private val getProductListUseCase: GetProductListUseCase
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

    private var isLoadMoreVisible = true

    var isInitialize = true
        private set

    val adapter = ProductListAdapter(
        dateUtil = dateUtil,
        clickLoadMore = { clickLoadMore() },
        showProductDetail = { productId -> showProductDetail(productId) }
    )

    fun initSearchWord(word: String) {
        if (!isInitialize) {
            return
        }
        viewModelScope.launch {
            _searchWord.emit(word)
        }
        requestProductList(true)
    }

    fun getNextProductList() {
        if (isLoadMoreVisible || isLoading.value) {
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
            val result = getProductListUseCase(searchWord.value, 0, isInitialize, LIST_COUNT)
            if (result.isSuccess) {
                val currentList = if (isInitialize) emptyList() else adapter.currentList
                val productItems = result.getOrThrow()
                if (isLoadMoreVisible && productItems.size == LIST_COUNT) {
                    adapter.submitList(currentList.plus(productItems).plus(ProductReadMore))
                } else {
                    adapter.submitList(currentList.plus(productItems))
                }
                _isEmptyResult.emit(productItems.isEmpty())
            }
            setLoadingState(false, isInitialize)
        }
    }

    private fun clickLoadMore() {
        isLoadMoreVisible = false
        adapter.submitList(adapter.currentList.filterIsInstance<ProductItem>())
        requestProductList(false)
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
        private const val LIST_COUNT = 20
    }
}
