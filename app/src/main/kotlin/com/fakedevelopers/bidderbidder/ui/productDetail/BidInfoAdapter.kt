package com.fakedevelopers.bidderbidder.ui.productDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec
import com.fakedevelopers.bidderbidder.databinding.RecyclerBidInfoBinding

class BidInfoAdapter : ListAdapter<BidInfo, BidInfoAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerBidInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BidInfo) {
            binding.textviewRank.text = binding.root.context.getString(R.string.product_detail_rank, item.index)
            if (item.index == 1) {
                binding.textviewRank.setTextColor(binding.root.context.getColor(R.color.bidderbidder_primary))
            }
            binding.textviewNickname.text = item.userNickname
            binding.textviewBid.text =
                if (item.bid == -1L) {
                    binding.root.context.getString(R.string.product_detail_secret_rank)
                } else {
                    dec.format(item.bid)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerBidInfoBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_bid_info, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BidInfo>() {
            override fun areItemsTheSame(oldItem: BidInfo, newItem: BidInfo) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: BidInfo, newItem: BidInfo) =
                oldItem == newItem
        }
    }
}
