package com.fakedevelopers.presentation.ui.productEditor.albumSelect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.RecyclerAlbumSelectBinding
import com.fakedevelopers.presentation.model.AlbumInfo

class AlbumSelectAdapter(
    private val onClick: (String) -> Unit
) : ListAdapter<AlbumInfo, AlbumSelectAdapter.ViewHolder>(diffUtil) {

    class ViewHolder(
        private val binding: RecyclerAlbumSelectBinding,
        private val onClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlbumInfo) {
            binding.albumInfo = item
            binding.root.setOnClickListener {
                onClick(item.path)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.recycler_album_select,
                parent,
                false
            ),
            onClick
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<AlbumInfo>() {
            override fun areItemsTheSame(oldItem: AlbumInfo, newItem: AlbumInfo) =
                oldItem.path == newItem.path

            override fun areContentsTheSame(oldItem: AlbumInfo, newItem: AlbumInfo) =
                oldItem == newItem
        }
    }
}
