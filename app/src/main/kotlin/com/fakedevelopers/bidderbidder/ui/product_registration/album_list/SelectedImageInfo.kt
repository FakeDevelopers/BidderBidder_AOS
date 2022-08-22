package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedImageInfo(
    var uris: MutableList<String> = mutableListOf(),
    val changeBitmaps: HashMap<String, BitmapInfo> = hashMapOf()
) : Parcelable
