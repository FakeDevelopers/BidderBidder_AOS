package com.fakedevelopers.bidderbidder.ui.product_registration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductRegistrationDto(
    var urlList: List<String>,
    val title: String,
    val hopePrice: String,
    val openingBid: String,
    val tick: String,
    val expiration: String,
    val content: String
) : Parcelable
