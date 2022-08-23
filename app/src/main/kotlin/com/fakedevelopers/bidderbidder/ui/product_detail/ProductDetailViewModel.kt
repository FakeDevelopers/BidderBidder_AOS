package com.fakedevelopers.bidderbidder.ui.product_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductDetailRepository
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductDetailRepository
) : ViewModel() {

    private val _productDetailResponse = MutableEventFlow<Response<ProductDetailDto>>()

    val productDetailResponse = _productDetailResponse.asEventFlow()

    fun productDetailRequest(productId: Long) {
        viewModelScope.launch {
            _productDetailResponse.emit(repository.getProductDetail(productId))
        }
    }
}
