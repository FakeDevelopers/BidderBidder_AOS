package com.fakedevelopers.presentation.ui.productSearch

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
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

    private var searchWord = ""

    inner class ViewHolder(
        private val binding: RecyclerProductSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            val start = item.indexOf(searchWord)
            binding.textviewSearchResult.text = SpannableStringBuilder(item).apply {
                setSpan(
                    ForegroundColorSpan(binding.root.context.getColor(R.color.bidderbidder_primary)),
                    start,
                    start + searchWord.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
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

    fun setSearchWord(searchWord: String) {
        this.searchWord = searchWord
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
