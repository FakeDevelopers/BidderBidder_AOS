package com.fakedevelopers.presentation.ui.productSearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.RecyclerProductSearchPopularBinding

class SearchPopularAdapter(
    private val searchEvent: (String) -> Unit
) : ListAdapter<String, SearchPopularAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerProductSearchPopularBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, position: Int) {
            binding.textviewSearchPopularRank.text =
                binding.root.context.getString(R.string.product_search_popular_rank, position + 1)
            binding.textviewSearchPopular.text = item
            binding.cardviewSearchPopular.setOnClickListener {
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
