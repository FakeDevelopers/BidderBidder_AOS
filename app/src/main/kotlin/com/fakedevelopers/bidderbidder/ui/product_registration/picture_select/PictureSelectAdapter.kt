package com.fakedevelopers.bidderbidder.ui.product_registration.picture_select

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerPictureSelectBinding

class PictureSelectAdapter(
    private val setSelectedImageList: (String, Int, Boolean) -> Unit
) : ListAdapter<String, PictureSelectAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerPictureSelectBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, position: Int) {
            binding.imageviewPictureSelect.let { image ->
                Glide.with(context)
                    .load(item)
                    .placeholder(R.drawable.the_cat)
                    .error(R.drawable.error_cat)
                    .into(image)
                image.setOnClickListener {
                    val visibility =
                        if (binding.backgroundPictrueSelect.visibility == View.VISIBLE)
                            View.INVISIBLE
                        else
                            View.VISIBLE
                    binding.backgroundPictrueSelect.visibility = visibility
                    binding.textviewPictureSelectCount.visibility = visibility
                    setSelectedImageList(item, position, visibility == View.VISIBLE)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerPictureSelectBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_picture_select, parent, false)
            ),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String) =
                oldItem == newItem
        }
    }
}
