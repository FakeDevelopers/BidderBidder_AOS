package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.ProductDetailService
import com.fakedevelopers.domain.model.ProductDetailInfo
import com.fakedevelopers.domain.repository.ProductDetailRepository
import javax.inject.Inject

class ProductDetailRepositoryImpl @Inject constructor(
    private val service: ProductDetailService
) : ProductDetailRepository {

    override suspend fun getProductDetail(productId: Long): Result<ProductDetailInfo> =
        runCatching {
            service.getProductDetail(productId)
        }

    override suspend fun postProductBid(
        productId: Long,
        userId: Long,
        bid: Long
    ): Result<String> =
        runCatching {
            service.postProductBid(productId, userId, bid)
        }
}
