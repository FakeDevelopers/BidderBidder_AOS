package com.fakedevelopers.bidderbidder.ui.productList

data class ProductItem(
    val productId: Long,
    val thumbnail: String,
    val productTitle: String,
    val hopePrice: Long,
    val openingBid: Long,
    val tick: Int,
    val expirationDate: String,
    val bidderCount: Int
)
