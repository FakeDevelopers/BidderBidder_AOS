package com.fakedevelopers.domain.model

import okhttp3.MultipartBody

data class ProductEditorInfo(
    val productContent: String,
    val productTitle: String,
    val expirationDate: String,
    val hopePrice: String,
    val openingBid: String,
    val representPicture: String,
    val tick: String,
    val category: String,
    val files: List<MultipartBody.Part>
) {
    fun getMap(): Map<String, String> {
        return mapOf(
            "productContent" to productContent,
            "productTitle" to productTitle,
            "expirationDate" to expirationDate,
            "hopePrice" to hopePrice,
            "openingBid" to openingBid,
            "representPicture" to representPicture,
            "tick" to tick,
            "category" to category
        )
    }
}

data class ProductCategoryDto(
    val categoryId: Long,
    val categoryName: String,
    val parentCategoryId: Long?,
    val subCategories: List<ProductCategoryDto>
)
