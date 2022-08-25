package com.fakedevelopers.bidderbidder.ui.product_detail

data class ProductDetailDto(
    val bidderCount: Int,
    val bids: List<BidInfo>,
    val createdTime: String,
    val expirationDate: String,
    val hopePrice: Long,
    val images: List<String>,
    val openingBid: Long,
    val productContent: String,
    val productTitle: String,
    val tick: Long
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendLine("bidderCount = $bidderCount")
        sb.appendLine("createTime = $createdTime")
        sb.appendLine("expirationDate = $expirationDate")
        sb.appendLine("hopePrice = $hopePrice")
        sb.appendLine("images = ${images.joinToString(" ")}")
        sb.appendLine("openingBid = $openingBid")
        sb.appendLine("productContent = $productContent")
        sb.appendLine("productTitle = $productTitle")
        sb.appendLine("tick = $tick")
        return sb.toString()
    }
}
