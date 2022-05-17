package com.fakedevelopers.bidderbidder.ui.product_list

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.util.ImageLoader.loadProductImage
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListBinding
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListFooterBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ProductListAdapter(
    private val onClick: () -> Unit,
    private val getPriceInfo: (String) -> String
) : ListAdapter<ProductListDto, RecyclerView.ViewHolder>(diffUtil) {

    private var listSize = 0

    inner class ItemViewHolder(
        private val binding: RecyclerProductListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var timerTask: CountDownTimer
        fun bind(item: ProductListDto) {
            with(binding) {
                if (::timerTask.isInitialized) {
                    timerTask.cancel()
                }
                val timer = dateFormat.parse(item.expirationDate)!!.time - System.currentTimeMillis()
                timerTask = object : CountDownTimer(timer, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        (millisUntilFinished / 60000).let {
                            val minute = if (it > 0) "${it}분 " else ""
                            val second = if (it < 5) "${millisUntilFinished % 60000 / 1000}초 " else ""
                            textviewProductListExpire.text = "${minute}${second}후 마감"
                        }
                    }

                    override fun onFinish() {
                        textviewProductListExpire.text = "마감"
                    }
                }.start()
                loadProductImage(item.thumbnail) {
                    if (it != null) {
                        imageProductList.setImageBitmap(it)
                    }
                }
                textviewProductListTitle.text = item.boardTitle
                textviewProductListHopePrice.text = getPriceInfo(dec.format(item.hopePrice))
                textviewProductListOpeningBid.text = getPriceInfo(dec.format(item.openingBid))
                textviewProductListParticipant.text = if (item.bidderCount != 0) "${item.bidderCount}명 입찰" else ""
            }
        }
    }

    inner class FooterViewHolder(
        private val binding: RecyclerProductListFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.buttonLoadMore.setOnClickListener {
                binding.buttonLoadMore.visibility = View.GONE
                onClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        LayoutInflater.from(parent.context).let {
            return if (viewType == TYPE_ITEM) {
                ItemViewHolder(
                    RecyclerProductListBinding.bind(
                        it.inflate(R.layout.recycler_product_list, parent, false)
                    )
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
        if (position == itemCount - 1) TYPE_FOOTER else TYPE_ITEM

    override fun getItemCount() = if (listSize == 0) super.getItemCount() else listSize

    override fun submitList(list: List<ProductListDto>?) {
        if (!list.isNullOrEmpty()) {
            listSize = list.size + 1
        }
        super.submitList(list)
    }

    companion object {
        const val TYPE_ITEM = 1
        const val TYPE_FOOTER = 2

        val dec = DecimalFormat("#,###")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val diffUtil = object : DiffUtil.ItemCallback<ProductListDto>() {
            override fun areItemsTheSame(oldItem: ProductListDto, newItem: ProductListDto) =
                oldItem.boardId == newItem.boardId

            override fun areContentsTheSame(oldItem: ProductListDto, newItem: ProductListDto) =
                oldItem == newItem
        }
    }
}
