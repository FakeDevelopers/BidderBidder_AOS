package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ProductDetailRepository
import javax.inject.Inject

class GetProductDetailUseCase @Inject constructor(
    private val repository: ProductDetailRepository
) {
    suspend operator fun invoke(productId: Long) = repository.getProductDetail(productId)
}
