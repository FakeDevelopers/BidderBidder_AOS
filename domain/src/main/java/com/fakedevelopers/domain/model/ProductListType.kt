package com.fakedevelopers.domain.model

sealed interface ProductListType {
    fun isItemTheSame(item: ProductListType): Boolean
}

data class ProductItem(
    val productId: Long,
    val thumbnail: String,
    val productTitle: String,
    val hopePrice: Long,
    val openingBid: Long,
    val tick: Int,
    val expirationDate: String,
    val bidderCount: Int
) : ProductListType {
    override fun isItemTheSame(item: ProductListType) = (item as? ProductItem)?.productId == productId
}

object ProductReadMore : ProductListType {
    override fun isItemTheSame(item: ProductListType) = true
}
