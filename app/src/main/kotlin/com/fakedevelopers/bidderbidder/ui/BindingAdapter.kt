package com.fakedevelopers.bidderbidder.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R

@BindingAdapter("image_uri")
fun bindImageUri(view: ImageView, uri: String?) {
    Glide.with(view.context)
        .load(uri)
        .placeholder(R.drawable.the_cat)
        .error(R.drawable.error_cat)
        .into(view)
}
