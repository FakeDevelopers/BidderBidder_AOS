package com.fakedevelopers.presentation.ui.productRegistration

import android.os.Parcelable
import com.fakedevelopers.presentation.ui.productDetail.ProductDetailDto
import com.fakedevelopers.presentation.ui.productRegistration.albumList.SelectedImageInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductRegistrationDto(
    val selectedImageInfo: SelectedImageInfo,
    val title: String,
    val hopePrice: String,
    val openingBid: String,
    val tick: String,
    val expiration: String,
    val content: String,
    val categoryId: Long
) : Parcelable {
    constructor(categoryId: Long, productDetailDto: ProductDetailDto) : this(
        selectedImageInfo = SelectedImageInfo(),
        title = productDetailDto.productTitle,
        hopePrice = productDetailDto.hopePrice.toString(),
        openingBid = productDetailDto.openingBid.toString(),
        tick = productDetailDto.tick.toString(),
        expiration = productDetailDto.expirationDate,
        content = productDetailDto.productContent,
        categoryId = categoryId
    )
}

data class ProductCategoryDto(
    val categoryId: Long,
    val categoryName: String,
    val parentCategoryId: Long,
    val subCategories: List<ProductCategoryDto>
)
