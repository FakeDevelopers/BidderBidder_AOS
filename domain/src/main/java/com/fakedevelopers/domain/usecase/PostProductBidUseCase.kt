package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ProductDetailRepository
import javax.inject.Inject

class PostProductBidUseCase @Inject constructor(
    private val repository: ProductDetailRepository
) {
    suspend operator fun invoke(productId: Long, userId: Long, bid: Long) = repository.postProductBid(productId, userId, bid)
}
