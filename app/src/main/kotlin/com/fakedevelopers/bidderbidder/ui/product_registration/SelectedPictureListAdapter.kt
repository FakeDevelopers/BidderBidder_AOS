package com.fakedevelopers.bidderbidder.ui.product_registration

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerProductRegistrationBinding

class SelectedPictureListAdapter(
    private val deleteSelectedImage: (String) -> Unit,
    private val findSelectedImageIndex: (String) -> Int,
    private val swapComplete: () -> Unit = {},
    private val swapSelectedImage: (Int, Int) -> Unit = { _, _ -> }
) : ListAdapter<String, SelectedPictureListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerProductRegistrationBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            setRepresentImage(findSelectedImageIndex(item) == 0)
            Glide.with(context)
                .load(item)
                .placeholder(R.drawable.the_cat)
                .error(R.drawable.error_cat)
                .into(binding.imageviewProductRegistration)
            // 선택 사진 터치 시 제거
            binding.imageviewProductRegistration.setOnClickListener {
                deleteSelectedImage(item)
            }
        }

        fun setRepresentImage(state: Boolean) {
            binding.textviewProductRegistration.visibility =
                if (state)
                    View.VISIBLE
                else
                    View.INVISIBLE
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
        swapSelectedImage(fromPosition, toPosition)
    }

    fun changeMoveEvent() {
        swapComplete()
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
