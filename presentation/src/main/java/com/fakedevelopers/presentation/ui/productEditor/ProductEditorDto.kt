package com.fakedevelopers.presentation.ui.productEditor

import android.os.Parcelable
import com.fakedevelopers.domain.model.ProductDetailInfo
import com.fakedevelopers.presentation.ui.productEditor.albumList.SelectedImageInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductEditorDto(
    val selectedImageInfo: SelectedImageInfo,
    val title: String,
    val hopePrice: String,
    val openingBid: String,
    val tick: String,
    val expiration: String,
    val content: String,
    val categoryId: Long
) : Parcelable {
    constructor(categoryId: Long, productDetailInfo: ProductDetailInfo) : this(
        selectedImageInfo = SelectedImageInfo(),
        title = productDetailInfo.productTitle,
        hopePrice = productDetailInfo.hopePrice.toString(),
        openingBid = productDetailInfo.openingBid.toString(),
        tick = productDetailInfo.tick.toString(),
        expiration = productDetailInfo.expirationDate,
        content = productDetailInfo.productContent,
        categoryId = categoryId
    )
}

data class ProductCategoryDto(
    val categoryId: Long,
    val categoryName: String,
    val parentCategoryId: Long,
    val subCategories: List<ProductCategoryDto>
)
