package com.fakedevelopers.bidderbidder.ui.product_detail

import androidx.lifecycle.ViewModel
import com.fakedevelopers.bidderbidder.api.repository.ProductDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductDetailRepository
) : ViewModel() {
}
