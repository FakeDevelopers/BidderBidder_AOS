package com.fakedevelopers.presentation.ui.productRegistration.albumList

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.RecyclerPictureSelectBinding
import com.fakedevelopers.presentation.ui.util.ContentResolverUtil
import com.fakedevelopers.presentation.ui.util.GlideRequestListener

class AlbumListAdapter(
    private val contentResolverUtil: ContentResolverUtil,
    private val findSelectedImageIndex: (String) -> Int,
    private val sendErrorToast: () -> Unit,
    private val showViewPager: (String) -> Unit,
    private val setSelectedImageList: (String, Boolean) -> Unit
) : ListAdapter<AlbumItem, AlbumListAdapter.ViewHolder>(diffUtil) {

    private val viewHolders = hashSetOf<ViewHolder>()

    inner class ViewHolder(
        private val binding: RecyclerPictureSelectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isErrorImage = false
        private var currentUri = ""
        var isSelected = false
            private set

        fun bind(item: AlbumItem) {
            currentUri = item.uri
            Glide.with(binding.root.context)
                .load(item.uri)
                .placeholder(R.drawable.the_cat)
                .error(R.drawable.error_cat)
                .signature(ObjectKey(item.modifiedTime))
                .listener(
                    GlideRequestListener(
                        loadFailed = { isErrorImage = true },
                        resourceReady = { isErrorImage = false }
                    )
                )
                .into(binding.imageviewPictureSelect)
            setPictureSelectCount()
            binding.layoutPictureSelectChoice.setOnClickListener {
                if (isValidImage(item.uri)) {
                    isSelected = !isSelected
                    setSelectedImageList(item.uri, isSelected)
                    setPictureSelectCount()
                    if (!isSelected) {
                        refreshSelectedOrder()
                    }
                } else {
                    sendErrorToast()
                }
            }
            // 뷰 페이저
            binding.layoutPictureSelectPager.setOnClickListener {
                if (isValidImage(item.uri)) {
                    showViewPager(item.uri)
                } else {
                    sendErrorToast()
                }
            }
        }

        fun setPictureSelectCount() {
            val selectedCount = findSelectedImageIndex(currentUri) + 1
            isSelected = selectedCount != 0
            binding.backgroundPictrueSelect.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            binding.textviewPictureSelectCount.apply {
                text = if (this@ViewHolder.isSelected) {
                    setBackgroundResource(R.drawable.shape_picture_select_count)
                    selectedCount.toString()
                } else {
                    setBackgroundResource(R.drawable.shape_picture_select_empty)
                    ""
                }
            }
        }

        private fun isValidImage(item: String) = !isErrorImage && contentResolverUtil.isExist(Uri.parse(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerPictureSelectBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_picture_select, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        viewHolders.add(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        viewHolders.remove(holder)
    }

    fun refreshSelectedOrder() {
        viewHolders.filter { it.isSelected }.forEach { holder ->
            holder.setPictureSelectCount()
        }
    }

    fun refreshAll() {
        viewHolders.forEach { holder ->
            holder.setPictureSelectCount()
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AlbumItem>() {
            override fun areItemsTheSame(oldItem: AlbumItem, newItem: AlbumItem) =
                oldItem.uri == newItem.uri

            override fun areContentsTheSame(oldItem: AlbumItem, newItem: AlbumItem) =
                oldItem == newItem
        }
    }
}
