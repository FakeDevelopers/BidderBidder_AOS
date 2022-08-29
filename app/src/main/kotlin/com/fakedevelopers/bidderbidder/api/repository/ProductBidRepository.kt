package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ProductBidService
import javax.inject.Inject

class ProductBidRepository @Inject constructor(
    private val service: ProductBidService
) {
    suspend fun postProductBid(
        productId: Long,
        userId: Long,
        bid: Long
    ) {
        service.postProductBid(productId, userId, bid)
    }
}
