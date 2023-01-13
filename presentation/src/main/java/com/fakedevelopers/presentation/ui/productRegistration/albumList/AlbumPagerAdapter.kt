package com.fakedevelopers.presentation.ui.productRegistration.albumList

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.RecyclerAlbumPagerBinding
import com.fakedevelopers.presentation.ui.productRegistration.albumList.AlbumListAdapter.Companion.diffUtil
import com.fakedevelopers.presentation.ui.util.ContentResolverUtil
import com.fakedevelopers.presentation.ui.util.GlideRequestListener

class AlbumPagerAdapter(
    private val contentResolverUtil: ContentResolverUtil,
    private val sendErrorToast: () -> Unit,
    private val getEditedImage: (String) -> BitmapInfo?,
    private val setSelectedImageList: (String) -> Unit
) : ListAdapter<AlbumItem, AlbumPagerAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(
        private val binding: RecyclerAlbumPagerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private var isErrorImage = false
        fun bind(item: AlbumItem) {
            getEditedImage(item.uri)?.let { bitmapInfo ->
                binding.imageviewAlbumPager.rotation = bitmapInfo.degree
            } ?: run { binding.imageviewAlbumPager.rotation = 0f }
            setGlide(item)
            binding.layoutAlbumPager.setOnClickListener {
                if (isValidImage(item.uri)) {
                    setSelectedImageList(item.uri)
                } else {
                    sendErrorToast()
                }
            }
        }

        private fun setGlide(item: AlbumItem) {
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
                .into(binding.imageviewAlbumPager)
        }

        private fun isValidImage(item: String) = !isErrorImage && contentResolverUtil.isExist(Uri.parse(item))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerAlbumPagerBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_album_pager, parent, false)
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
