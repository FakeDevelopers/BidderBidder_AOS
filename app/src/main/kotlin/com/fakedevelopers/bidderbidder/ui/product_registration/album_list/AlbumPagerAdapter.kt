package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerAlbumPagerBinding
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListAdapter.Companion.diffUtil
import com.fakedevelopers.bidderbidder.ui.util.ContentResolverUtil
import com.fakedevelopers.bidderbidder.ui.util.GlideRequestListener

class AlbumPagerAdapter(
    private val sendErrorToast: () -> Unit,
    private val getEditedImage: (String) -> BitmapInfo?,
    private val setSelectedImageList: (String) -> Unit
) : ListAdapter<Pair<String, Long>, AlbumPagerAdapter.ViewHolder>(diffUtil) {
    private lateinit var contentResolverUtil: ContentResolverUtil

    inner class ViewHolder(
        private val binding: RecyclerAlbumPagerBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private var isErrorImage = false
        fun bind(item: Pair<String, Long>) {
            // 수정된 이미지가 있다면 그걸 띄운다.
            getEditedImage(item.first)?.let { bitmapInfo ->
                binding.imageviewAlbumPager.setImageBitmap(bitmapInfo.bitmap)
            } ?: setGlide(item)
            binding.layoutAlbumPager.setOnClickListener {
                if (isValidImage(item.first)) {
                    setSelectedImageList(item.first)
                } else {
                    sendErrorToast()
                }
            }
        }

        private fun setGlide(item: Pair<String, Long>) {
            Glide.with(context)
                .load(item.first)
                .placeholder(R.drawable.the_cat)
                .error(R.drawable.error_cat)
                .signature(ObjectKey(item.second))
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
        contentResolverUtil = ContentResolverUtil(parent.context)
        return ViewHolder(
            RecyclerAlbumPagerBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_album_pager, parent, false)
            ),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
