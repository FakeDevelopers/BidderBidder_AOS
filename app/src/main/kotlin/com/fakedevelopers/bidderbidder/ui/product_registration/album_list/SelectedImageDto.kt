package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedImageDto(
    var uris: MutableList<String> = mutableListOf(),
    val changeBitmaps: HashMap<String, Bitmap> = hashMapOf()
) : Parcelable
