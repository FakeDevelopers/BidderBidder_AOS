package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BitmapInfo(
    var bitmap: Bitmap,
    val extension: String,
    val mimeType: String,
    var degree: Float
) : Parcelable
