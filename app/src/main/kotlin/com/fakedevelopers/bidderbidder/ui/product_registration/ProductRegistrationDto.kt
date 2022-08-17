package com.fakedevelopers.bidderbidder.ui.product_registration

import android.os.Parcelable
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.SelectedImageDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductRegistrationDto(
    val selectedImageDto: SelectedImageDto,
    val title: String,
    val hopePrice: String,
    val openingBid: String,
    val tick: String,
    val expiration: String,
    val content: String
) : Parcelable
