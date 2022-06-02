package com.fakedevelopers.bidderbidder.ui.product_list

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListBinding
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListFooterBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductListAdapter(
    private val onClick: () -> Unit,
    private val getPriceInfo: (String) -> String
) : ListAdapter<ProductListDto, RecyclerView.ViewHolder>(diffUtil) {

    private var listSize = 0

    inner class ItemViewHolder(
        private val binding: RecyclerProductListBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var timerTask: CountDownTimer
        fun bind(item: ProductListDto) {
            with(binding) {
                if (::timerTask.isInitialized) {
                    timerTask.cancel()
                }
                val timer = dateFormat.parse(item.expirationDate)!!.time - Date(System.currentTimeMillis()).time
                timerTask = object : CountDownTimer(timer, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        textviewProductListExpire.text = getRemainTimeString(millisUntilFinished)
                    }

                    override fun onFinish() {
                        textviewProductListExpire.text = "마감"
                    }
                }.start()
                Glide.with(context)
                    .load(item.thumbnail)
                    .placeholder(R.drawable.the_cat)
                    .error(R.drawable.error_cat)
                    .into(imageProductList)
                textviewProductListTitle.text = item.boardTitle
                textviewProductListHopePrice.text = getPriceInfo(dec.format(item.hopePrice))
                textviewProductListOpeningBid.text = getPriceInfo(dec.format(item.openingBid))
                textviewProductListParticipant.text = if (item.bidderCount != 0) "${item.bidderCount}명 입찰" else ""
            }
        }
        private fun getRemainTimeString(millisUntilFinished: Long): String {
            val totalMinute = millisUntilFinished / 60000
            val day = totalMinute / 1440
            val hour = totalMinute % 1440 / 60
            val remainTimeString = StringBuilder("마감까지 ")
            // 일
            if (day > 0) {
                remainTimeString.append("${day}일 ")
            }
            // 시간
            if (hour != 0L) {
                remainTimeString.append("${hour}시간 ")
            }
            // 분, 초
            if (day == 0L && hour < 3) {
                val minute = totalMinute % 1440 % 60
                if (minute != 0L) {
                    remainTimeString.append("${minute}분 ")
                }
                if (hour == 0L && minute < 5) {
                    remainTimeString.append("${millisUntilFinished % 60000 / 1000}초")
                }
            }
            return remainTimeString.toString()
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
