package com.fakedevelopers.bidderbidder.ui.product_search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductSearchPopularBinding

class SearchPopularAdapter(
    private val searchEvent: (String) -> Unit
) : ListAdapter<String, SearchPopularAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerProductSearchPopularBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.textviewSearchPopular.text = item
            binding.layoutSearchPopular.setOnClickListener {
                searchEvent(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerProductSearchPopularBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_product_search_popular, parent, false)
            )
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
