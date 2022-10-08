package com.fakedevelopers.bidderbidder.ui.productRegistration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductRegistrationBinding

class SelectedPictureListAdapter(
    private val deleteSelectedImage: (String) -> Unit,
    private val findSelectedImageIndex: (String) -> Int,
    private val swapComplete: (() -> Unit)? = null,
    private val swapSelectedImage: ((Int, Int) -> Unit)? = null
) : ListAdapter<String, SelectedPictureListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerProductRegistrationBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            setRepresentImage(findSelectedImageIndex(item) == 0)
            // 얘는 따로 시그니처를 쓰지 않았습니다.
            // 시그니처를 쓸라면 코드를 많이 바까야 해요 ㅎㅎ..
            Glide.with(context)
                .load(item)
                .placeholder(R.drawable.the_cat)
                .error(R.drawable.error_cat)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imageviewProductRegistration)
            // 선택 사진 터치 시 제거
            binding.imageviewProductRegistration.setOnClickListener {
                deleteSelectedImage(item)
            }
        }

        fun setRepresentImage(state: Boolean) {
            binding.textviewProductRegistration.visibility =
                if (state) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerProductRegistrationBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_product_registration, parent, false)
            ),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun onItemDragMove(fromPosition: Int, toPosition: Int) {
        swapSelectedImage?.invoke(fromPosition, toPosition)
    }

    fun changeMoveEvent() {
        swapComplete?.invoke()
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
