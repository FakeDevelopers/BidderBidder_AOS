package com.fakedevelopers.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModificationDto(
    val productId: Long = 0L,
    val expirationDate: String = "",
    val hopePrice: Long = 0L,
    val images: List<String> = emptyList(),
    val openingBid: Long = 0L,
    val productContent: String = "",
    val productTitle: String = "",
    val tick: Int = 0
) : Parcelable
