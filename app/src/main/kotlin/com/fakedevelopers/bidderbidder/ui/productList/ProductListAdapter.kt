package com.fakedevelopers.bidderbidder.ui.productList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListBinding
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListFooterBinding
import com.fakedevelopers.bidderbidder.ui.productList.ProductListViewModel.Companion.LIST_COUNT
import com.fakedevelopers.bidderbidder.ui.util.ExpirationTimerTask

class ProductListAdapter(
    private val clickLoadMore: () -> Unit,
    private val clickProductDetail: (Long) -> Unit,
    private val isReadMoreVisible: () -> Boolean
) : ListAdapter<ProductListDto, RecyclerView.ViewHolder>(diffUtil) {

    private var listSize = 0

    inner class ItemViewHolder(
        private val binding: RecyclerProductListBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var timerTask: ExpirationTimerTask
        fun bind(item: ProductListDto) {
            with(binding) {
                if (::timerTask.isInitialized) {
                    timerTask.cancel()
                }
                timerTask = ExpirationTimerTask(
                    item.expirationDate,
                    1000,
                    tick = { remainTimeString -> textviewProductListExpire.text = "마감까지 $remainTimeString" },
                    finish = { textviewProductListExpire.text = "마감" }
                )
                timerTask.start()
                Glide.with(context)
                    .load(BASE_URL + item.thumbnail)
                    .placeholder(R.drawable.the_cat)
                    .error(R.drawable.error_cat)
                    .into(imageProductList)
                textviewProductListTitle.text = item.productTitle
                if (item.hopePrice == 0L) {
                    hopePrice.visibility = View.GONE
                    textviewProductListHopePrice.visibility = View.GONE
                } else {
                    hopePrice.visibility = View.VISIBLE
                    textviewProductListHopePrice.visibility = View.VISIBLE
                    textviewProductListHopePrice.text = "${dec.format(item.hopePrice)}원"
                }

                textviewProductListOpeningBid.text = "${dec.format(item.openingBid)}원"
                textviewProductListParticipant.text = if (item.bidderCount != 0) "${item.bidderCount}명 입찰" else ""
                // 상품 상세 정보로 넘어가기
                layoutProductList.setOnClickListener {
                    clickProductDetail(item.productId)
                }
            }
        }
    }

    inner class FooterViewHolder(
        private val binding: RecyclerProductListFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.buttonLoadMore.run {
                visibility = if (isReadMoreVisible()) View.VISIBLE else View.GONE
                setOnClickListener {
                    binding.buttonLoadMore.visibility = View.GONE
                    clickLoadMore()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        LayoutInflater.from(parent.context).let {
            return if (viewType == TYPE_ITEM) {
                ItemViewHolder(
                    RecyclerProductListBinding.bind(
                        it.inflate(R.layout.recycler_product_list, parent, false)
                    ),
                    parent.context
                )
            } else {
                FooterViewHolder(
                    RecyclerProductListFooterBinding.bind(
                        it.inflate(R.layout.recycler_product_list_footer, parent, false)
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FooterViewHolder -> holder.bind()
            is ItemViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int) =
        if (position <= LIST_COUNT && position == itemCount - 1) TYPE_FOOTER else TYPE_ITEM

    override fun getItemCount() = if (listSize == 0 || listSize > LIST_COUNT) super.getItemCount() else listSize

    override fun submitList(list: List<ProductListDto>?) {
        if (!list.isNullOrEmpty()) {
            listSize = list.size + 1
        }
        super.submitList(list)
    }

    companion object {
        const val TYPE_ITEM = 1
        const val TYPE_FOOTER = 2

        val diffUtil = object : DiffUtil.ItemCallback<ProductListDto>() {
            override fun areItemsTheSame(oldItem: ProductListDto, newItem: ProductListDto) =
                oldItem.productId == newItem.productId

            override fun areContentsTheSame(oldItem: ProductListDto, newItem: ProductListDto) =
                oldItem == newItem
        }
    }
}
