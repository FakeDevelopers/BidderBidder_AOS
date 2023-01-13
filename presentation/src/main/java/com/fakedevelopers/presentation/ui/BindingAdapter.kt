package com.fakedevelopers.presentation.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.fakedevelopers.presentation.R

@BindingAdapter("image_uri")
fun bindImageUri(view: ImageView, uri: String?) {
    Glide.with(view.context)
        .load(uri)
        .placeholder(R.drawable.the_cat)
        .error(R.drawable.error_cat)
        .into(view)
}
