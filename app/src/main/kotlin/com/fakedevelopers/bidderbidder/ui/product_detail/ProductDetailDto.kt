package com.fakedevelopers.bidderbidder.ui.product_detail

data class ProductDetailDto(
    val bidderCount: Int,
    val createdTime: String,
    val expirationDate: String,
    val hopePrice: Int,
    val images: List<String>,
    val openingBid: Int,
    val productContent: String,
    val productTitle: String,
    val tick: Int
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
        return super.toString()
    }
}
