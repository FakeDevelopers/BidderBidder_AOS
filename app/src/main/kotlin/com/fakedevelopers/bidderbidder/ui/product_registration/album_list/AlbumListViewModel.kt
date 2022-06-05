package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.product_registration.SelectedPictureListAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections

class AlbumListViewModel : ViewModel() {

    private val imageList = MutableStateFlow<List<String>>(emptyList())
    private val currentAlbum = MutableStateFlow("")
    private val _selectedImageList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private val _onListChange = MutableSharedFlow<Boolean>()
    private lateinit var allImages: Map<String, MutableList<String>>

    val selectedImageList: StateFlow<List<String>> get() = _selectedImageList
    val onListChange: SharedFlow<Boolean> get() = _onListChange
    val albumListAdapter = AlbumListAdapter(
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        setScrollFlag = { setScrollFlag() }
    ) { uri, state ->
        setSelectedState(uri, state)
    }
    val selectedPictureAdapter = SelectedPictureListAdapter(
        deleteSelectedImage = { setSelectedState(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        swapComplete = { setAlbumList() }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }
    var scrollToTopFlag = false

    fun setAlbumList(albumName: String = currentAlbum.value) {
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

    fun initSelectedImageList(list: List<String>) {
        viewModelScope.launch {
            _selectedImageList.emit(list.toMutableList())
            setSelectedImageList()
        }
    }

    fun initAlbumInfo(map: Map<String, MutableList<String>>) {
        allImages = map
    }

    fun setScrollFlag() {
        scrollToTopFlag = !scrollToTopFlag
    }

    private fun setSelectedImageList() {
        selectedPictureAdapter.submitList(_selectedImageList.value.toList())
        setAlbumList()
        viewModelScope.launch {
            Logger.i("여긴가나")
            _onListChange.emit(true)
        }
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(_selectedImageList.value, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(_selectedImageList.value, i, i - 1)
            }
        }
        selectedPictureAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    private fun findSelectedImageIndex(uri: String) = _selectedImageList.value.indexOf(uri)

    private fun setSelectedState(uri: String, state: Boolean = false) {
        if (state) {
            _selectedImageList.value.add(uri)
        } else {
            _selectedImageList.value.remove(uri)
            // 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
            if (_selectedImageList.value.isNotEmpty()) {
                selectedPictureAdapter.notifyItemChanged(1)
            }
        }
        setSelectedImageList()
    }
}
