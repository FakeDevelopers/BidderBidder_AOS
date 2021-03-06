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
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dec
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListBinding
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductListFooterBinding
import com.fakedevelopers.bidderbidder.ui.product_list.ProductListViewModel.Companion.LIST_COUNT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class ProductListAdapter(
    private val onClick: () -> Unit,
    private val isReadMoreVisible: () -> Boolean,
    private val getPriceInfo: (String) -> String
) : ListAdapter<ProductListDto, RecyclerView.ViewHolder>(diffUtil) {

    private var listSize = 0
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm").apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

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
                timerTask = object : CountDownTimer(getRemainTimeMillisecond(item.expirationDate), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        textviewProductListExpire.text = getRemainTimeString(millisUntilFinished)
                    }

                    override fun onFinish() {
                        textviewProductListExpire.text = "??????"
                    }
                }.start()
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
                    textviewProductListHopePrice.text = getPriceInfo(dec.format(item.hopePrice))
                }
                textviewProductListOpeningBid.text = getPriceInfo(dec.format(item.openingBid))
                textviewProductListParticipant.text = if (item.bidderCount != 0) "${item.bidderCount}??? ??????" else ""
            }
        }

        private fun getRemainTimeMillisecond(expirationDate: String) =
            dateFormat.parse(expirationDate)!!.time - dateFormat.parse(dateFormat.format(Date()))!!.time

        private fun getRemainTimeString(millisUntilFinished: Long): String {
            val totalMinute = millisUntilFinished / 60000
            val day = totalMinute / 1440
            val hour = totalMinute % 1440 / 60
            val remainTimeString = StringBuilder("???????????? ")
            // ???
            if (day > 0) {
                remainTimeString.append("${day}??? ")
            }
            // ??????
            if (hour != 0L) {
                remainTimeString.append("${hour}?????? ")
            }
            // ???, ???
            if (day == 0L && hour < 3) {
                val minute = totalMinute % 60
                if (minute != 0L) {
                    remainTimeString.append("${minute}??? ")
                }
                if (hour == 0L && minute < 5) {
                    remainTimeString.append("${millisUntilFinished % 60000 / 1000}???")
                }
            }
            return remainTimeString.toString()
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
                    onClick()
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
