package com.fakedevelopers.bidderbidder.ui.productList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListBinding
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListFooterBinding
import com.fakedevelopers.bidderbidder.ui.util.DateUtil
import com.fakedevelopers.bidderbidder.ui.util.ExpirationTimerTask
import com.fakedevelopers.bidderbidder.ui.util.PriceUtil

class ProductListAdapter(
    private val dateUtil: DateUtil,
    private val clickLoadMore: () -> Unit,
    private val showProductDetail: (Long) -> Unit
) : ListAdapter<ProductListType, RecyclerView.ViewHolder>(diffUtil) {

    inner class ItemViewHolder(
        private val binding: RecyclerProductListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var timerTask: ExpirationTimerTask
        fun bind(item: ProductListType) {
            val productItem = item as? ProductItem ?: return
            with(binding) {
                if (::timerTask.isInitialized) {
                    timerTask.cancel()
                }
                timerTask = ExpirationTimerTask(
                    remainTime = dateUtil.getRemainTimeMillisecond(productItem.expirationDate) ?: 0L,
                    tick = { remainTime ->
                        textviewProductListExpire.text =
                            binding.root.context.getString(R.string.productlist_remain_time, remainTime)
                    },
                    finish = {
                        textviewProductListExpire.text =
                            binding.root.context.getString(R.string.productlist_expired)
                    }
                )
                timerTask.start()
                Glide.with(root.context)
                    .load(BASE_URL + productItem.thumbnail)
                    .placeholder(R.drawable.the_cat)
                    .error(R.drawable.error_cat)
                    .into(imageProductList)
                textviewProductListTitle.text = productItem.productTitle
                hopePrice.isVisible = productItem.hopePrice != 0L
                textviewProductListHopePrice.isVisible = productItem.hopePrice != 0L
                if (productItem.hopePrice != 0L) {
                    textviewProductListHopePrice.text = PriceUtil.numberToPrice(productItem.hopePrice)
                }
                textviewProductListOpeningBid.text = PriceUtil.numberToPrice(productItem.openingBid)
                textviewProductListParticipant.text =
                    if (productItem.bidderCount > 0) "${productItem.bidderCount}명 입찰" else ""
                // 상품 상세 정보로 넘어가기
                layoutProductList.setOnClickListener {
                    showProductDetail(productItem.productId)
                }
            }
        }
    }

    inner class FooterViewHolder(
        private val binding: RecyclerProductListFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.buttonLoadMore.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    visibility = View.GONE
                    clickLoadMore()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ITEM) {
            ItemViewHolder(
                RecyclerProductListBinding.bind(
                    layoutInflater.inflate(R.layout.recycler_product_list, parent, false)
                )
            )
        } else {
            FooterViewHolder(
                RecyclerProductListFooterBinding.bind(
                    layoutInflater.inflate(R.layout.recycler_product_list_footer, parent, false)
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FooterViewHolder -> holder.bind()
            is ItemViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int) =
        when (getItem(position)) {
            is ProductReadMore -> TYPE_FOOTER
            is ProductItem -> TYPE_ITEM
        }

    companion object {
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2

        private val diffUtil = object : DiffUtil.ItemCallback<ProductListType>() {
            override fun areItemsTheSame(oldItem: ProductListType, newItem: ProductListType) =
                oldItem.isItemTheSame(newItem)

            override fun areContentsTheSame(oldItem: ProductListType, newItem: ProductListType) =
                oldItem == newItem
        }
    }
}
