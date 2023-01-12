package com.fakedevelopers.presentation.ui.productSearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.RecyclerProductSearchHistoryBinding

class SearchHistoryAdapter(
    private val eraseHistory: (String) -> Unit,
    private val searchEvent: (String) -> Unit
) : ListAdapter<String, SearchHistoryAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerProductSearchHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            binding.textviewHistory.apply {
                text = item
                setOnClickListener {
                    searchEvent(item)
                }
            }
            binding.buttonErase.setOnClickListener {
                eraseHistory(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerProductSearchHistoryBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_product_search_history, parent, false)
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
