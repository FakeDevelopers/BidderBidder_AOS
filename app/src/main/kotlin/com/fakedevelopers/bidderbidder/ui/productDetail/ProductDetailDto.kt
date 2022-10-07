package com.fakedevelopers.bidderbidder.ui.productDetail

data class ProductDetailDto(
    val bidderCount: Int,
    val bids: List<BidInfo>,
    val createdDate: String,
    val expirationDate: String,
    val hopePrice: Long,
    val images: List<String>,
    val openingBid: Long,
    val productContent: String,
    val productTitle: String,
    val tick: Int
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.appendLine("bidderCount = $bidderCount")
        sb.appendLine("createDate = $createdDate")
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
