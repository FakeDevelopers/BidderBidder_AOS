package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.product_registration.SelectedPictureListAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Collections

class AlbumListViewModel : ViewModel() {

    private val imageList = MutableStateFlow<List<String>>(emptyList())
    private val currentAlbum = MutableStateFlow("")
    private val _selectedImageList = mutableListOf<String>()
    private lateinit var allImages: Map<String, MutableList<String>>

    val selectedImageList = MutableStateFlow<List<String>>(emptyList())
    val albumListAdapter = AlbumListAdapter(
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        setScrollFlag = { setScrollFlag() }
    ) { uri, state ->
        setSelectedState(uri, state)
    }
    val selectedPictureAdapter = SelectedPictureListAdapter(
        deleteSelectedImage = { setSelectedState(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        swapComplete = { swapComplete() }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }
    var scrollToTopFlag = false

    fun setList(albumName: String = currentAlbum.value) {
        viewModelScope.launch {
            // ViewHolder의 bind를 통해 사진 선택 순서를 표시한다.
            // 하지만 사진을 선택하는 행위는 리스트에 영향을 주지 않는다.
            // 리스트에 영향이 없으면 bind를 호출하지 않는다.
            // 그러므로 notifyDataSetChanged를 이용해 강제로 갱신 시켜야 한다.
            if (albumName == currentAlbum.value) {
                albumListAdapter.notifyDataSetChanged()
            } else {
                imageList.emit(allImages[albumName]!!)
                albumListAdapter.submitList(imageList.value.toMutableList())
                currentAlbum.emit(albumName)
            }
        }
    }

    fun setAllImages(map: Map<String, MutableList<String>>) {
        allImages = map
    }

    fun setScrollFlag() {
        scrollToTopFlag = !scrollToTopFlag
    }

    private fun swapComplete() {
        viewModelScope.launch {
            selectedImageList.emit(_selectedImageList.toList())
        }
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(_selectedImageList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(_selectedImageList, i, i - 1)
            }
        }
        selectedPictureAdapter.submitList(_selectedImageList.toList())
    }

    private fun findSelectedImageIndex(uri: String) = _selectedImageList.indexOf(uri)

    private fun setSelectedState(uri: String, state: Boolean = false) {
        if (state) {
            _selectedImageList.add(uri)
        } else {
            _selectedImageList.remove(uri)
            // 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
            if (_selectedImageList.isNotEmpty()) {
                selectedPictureAdapter.notifyItemChanged(1)
            }
        }
        selectedPictureAdapter.submitList(_selectedImageList.toList())
        viewModelScope.launch {
            selectedImageList.emit(_selectedImageList.toList())
        }
    }
}
