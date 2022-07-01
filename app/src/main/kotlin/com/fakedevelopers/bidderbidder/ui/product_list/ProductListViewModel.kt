package com.fakedevelopers.bidderbidder.ui.product_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductListRepository
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

    private val productList = MutableStateFlow(listOf<ProductListDto>())
    private val _isLoading = MutableStateFlow(false)
    private var _isInitialize = true
    private val _searchWord = MutableStateFlow("")
    private val isReadMoreVisible = MutableStateFlow(true)
    private val isLastProduct = MutableStateFlow(false)
    private val startNumber = MutableStateFlow(-1L)

    val isInitialize get() = _isInitialize
    val isLoading: StateFlow<Boolean> get() = _isLoading
    val searchWord: StateFlow<String> get() = _searchWord

    val adapter = ProductListAdapter(
        onClick = { clickReadMore() },
        isReadMoreVisible = { isReadMoreVisible.value }
    ) {
        "${it}원"
    }

    fun getNextProductList() {
        // 이미 로딩 중일 때
        // 더보기가 활성화 중일 때
        // 마지막 상품까지 불러왔을 때
        if (isLoading.value || isReadMoreVisible.value || isLastProduct.value) {
            return
        }

        requestProductList(false)
    }

    fun setInitializeState(state: Boolean) {
        _isInitialize = state
    }

    fun setSearchWord(word: String) {
        viewModelScope.launch {
            _searchWord.emit(word)
        }
    }

    fun requestProductList(isInitialize: Boolean) {
        // 최초 실행이거나 리프레쉬 중이면 startNumber를 초기화 한다.
        if (isInitialize) {
            isLastProduct.value = false
            startNumber.value = -1
        }
        _isInitialize = isInitialize
        viewModelScope.launch {
            // 추가하기 전에 로딩 띄우기
            setLoadingState(true)
            repository.postProductList(searchWord.value, 0, LIST_COUNT, startNumber.value).let {
                if (it.isSuccessful) {
                    val currentList = if (isInitialize) mutableListOf() else productList.value.toMutableList()
                    currentList.addAll(it.body()!!)
                    productList.emit(currentList)
                    if (productList.value.isNotEmpty()) {
                        startNumber.emit(productList.value[productList.value.size - 1].productId)
                    } else {
                        startNumber.emit(-1)
                    }
                    adapter.submitList(productList.value.toList())
                    // 요청한 것 보다 더 적게 받아오면 끝자락이라고 판단
                    if (it.body()!!.size < LIST_COUNT) {
                        isReadMoreVisible.value = false
                        isLastProduct.emit(true)
                    }
                } else {
                    Logger.e(it.errorBody().toString())
                }
            }
            setLoadingState(false)
        }
    }

    private fun clickReadMore() {
        isReadMoreVisible.value = false
        getNextProductList()
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
