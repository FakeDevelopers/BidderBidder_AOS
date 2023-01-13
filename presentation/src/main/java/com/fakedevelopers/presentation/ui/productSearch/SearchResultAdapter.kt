package com.fakedevelopers.presentation.ui.productSearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.RecyclerProductSearchResultBinding

class SearchResultAdapter(
    private val searchEvent: (String) -> Unit
) : ListAdapter<String, SearchResultAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerProductSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.textviewSearchResult.text = item
            binding.layoutSearchResult.setOnClickListener {
                searchEvent(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerProductSearchResultBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_product_search_result, parent, false)
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
