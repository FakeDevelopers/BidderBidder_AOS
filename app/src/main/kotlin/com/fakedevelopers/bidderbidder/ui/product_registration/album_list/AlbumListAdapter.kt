package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.RecyclerPictureSelectBinding
import com.fakedevelopers.bidderbidder.ui.util.ContentResolverUtil
import com.fakedevelopers.bidderbidder.ui.util.GlideRequestListener

class AlbumListAdapter(
    private val findSelectedImageIndex: (String) -> Int,
    private val sendErrorToast: () -> Unit,
    private val showViewPager: (String) -> Unit,
    private val setSelectedImageList: (String, Boolean) -> Unit
) : ListAdapter<Pair<String, Long>, AlbumListAdapter.ViewHolder>(diffUtil) {
    private lateinit var contentResolverUtil: ContentResolverUtil

    inner class ViewHolder(
        private val binding: RecyclerPictureSelectBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        private var isErrorImage = false
        fun bind(item: Pair<String, Long>) {
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
                .into(binding.imageviewPictureSelect)
            // 선택된 사진 리스트에 현재 item이 포함되어 있다면 표시해줍니다.
            findSelectedImageIndex(item.first).let { count ->
                setPictureSelectCount(count != -1, count + 1)
                binding.layoutPictureSelectChoice.setOnClickListener {
                    if (isValidImage(item.first)) {
                        setSelectedImageList(item.first, binding.backgroundPictrueSelect.visibility != View.VISIBLE)
                        setPictureSelectCount(binding.backgroundPictrueSelect.visibility != View.VISIBLE, count + 1)
                    } else {
                        sendErrorToast()
                    }
                }
            }
            // 뷰 페이저
            binding.layoutPictureSelectPager.setOnClickListener {
                if (isValidImage(item.first)) {
                    showViewPager(item.first)
                } else {
                    sendErrorToast()
                }
            }
        }

        private fun isValidImage(item: String) = !isErrorImage && contentResolverUtil.isExist(Uri.parse(item))

        private fun setPictureSelectCount(state: Boolean, count: Int) {
            binding.textviewPictureSelectCount.apply {
                text = if (state) {
                    binding.backgroundPictrueSelect.visibility = View.VISIBLE
                    setBackgroundResource(R.drawable.shape_picture_select_count)
                    count.toString()
                } else {
                    binding.backgroundPictrueSelect.visibility = View.INVISIBLE
                    setBackgroundResource(R.drawable.shape_picture_select_empty)
                    ""
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        contentResolverUtil = ContentResolverUtil(parent.context)
        return ViewHolder(
            RecyclerPictureSelectBinding.bind(
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_picture_select, parent, false)
            ),
            parent.context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Pair<String, Long>>() {
            override fun areItemsTheSame(oldItem: Pair<String, Long>, newItem: Pair<String, Long>) =
                oldItem.first == newItem.first && oldItem.second == newItem.second

            override fun areContentsTheSame(oldItem: Pair<String, Long>, newItem: Pair<String, Long>) =
                oldItem.first == newItem.first && oldItem.second == newItem.second
        }
    }
}
