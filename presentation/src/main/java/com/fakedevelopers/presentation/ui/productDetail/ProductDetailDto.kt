package com.fakedevelopers.presentation.ui.productDetail

data class ProductDetailDto(
    val bidderCount: Int = 0,
    val bids: List<BidInfo> = emptyList(),
    val createdDate: String = "",
    val expirationDate: String = "",
    val hopePrice: Long = 0L,
    val images: List<String> = emptyList(),
    val openingBid: Long = 0L,
    val productContent: String = "",
    val productTitle: String = "",
    val tick: Int = 0
)
