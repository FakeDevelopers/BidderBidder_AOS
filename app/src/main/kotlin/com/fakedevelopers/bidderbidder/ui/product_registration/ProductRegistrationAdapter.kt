package com.fakedevelopers.bidderbidder.ui.product_registration

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductRegistrationBinding

class ProductRegistrationAdapter : ListAdapter<String, ProductRegistrationAdapter.ViewHolder>(diffUtil) {

    class ViewHolder(
        private val binding: RecyclerProductRegistrationBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            Glide.with(context)
                .load(item)
                .placeholder(R.drawable.the_cat)
                .error(R.drawable.error_cat)
                .into(binding.imageviewProductRegistration)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerProductRegistrationBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_product_registration, parent, false)
            ),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
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
