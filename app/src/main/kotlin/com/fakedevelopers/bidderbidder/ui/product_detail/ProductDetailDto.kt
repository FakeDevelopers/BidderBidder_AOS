package com.fakedevelopers.bidderbidder.ui.product_detail

data class ProductDetailDto(
    val bidderCount: Int,
    val createTime: String,
    val expirationDate: String,
    val hopePrice: Int,
    val images: List<String>,
    val openingBid: Int,
    val productContent: String,
    val productTitle: String,
    val tick: Int
)
