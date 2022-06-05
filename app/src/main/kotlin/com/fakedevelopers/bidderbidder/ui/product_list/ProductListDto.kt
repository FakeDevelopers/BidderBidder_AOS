package com.fakedevelopers.bidderbidder.ui.product_list

data class ProductListDto(
    val productId: Long,
    val thumbnail: String,
    val productTitle: String,
    val hopePrice: Long,
    val openingBid: Long,
    val tick: Long,
    val expirationDate: String,
    val bidderCount: Int
)
