package com.fakedevelopers.bidderbidder.ui.productRegistration.albumList

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedImageInfo(
    var uris: MutableList<String> = mutableListOf(),
    val changeBitmaps: HashMap<String, BitmapInfo> = hashMapOf()
) : Parcelable
