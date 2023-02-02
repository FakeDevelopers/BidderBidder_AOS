package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.ProductDetailInfo

interface ProductDetailRepository {
    suspend fun getProductDetail(
        productId: Long
    ): Result<ProductDetailInfo>

    suspend fun postProductBid(
        productId: Long,
        userId: Long,
        bid: Long
    ): Result<String>
}
