package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.product_registration.SelectedPictureListAdapter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections

class AlbumListViewModel : ViewModel() {

    private val imageList = MutableStateFlow<List<String>>(emptyList())
    private val currentAlbum = MutableStateFlow("")
    private val _albumViewMode = MutableStateFlow(AlbumViewState.GRID)
    private val _onListChange = MutableSharedFlow<Boolean>()
    private val _pagerSelectedState = MutableSharedFlow<Boolean>()
    private val _selectedImageList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private val _selectErrorImage = MutableSharedFlow<Boolean>()
    private val _startViewPagerIndex = MutableSharedFlow<Int>()
    private var totalPictureCount = 0
    private lateinit var allImages: Map<String, MutableList<String>>

    val albumViewMode: StateFlow<AlbumViewState> get() = _albumViewMode
    val onListChange: SharedFlow<Boolean> get() = _onListChange
    val pagerSelectedState: SharedFlow<Boolean> get() = _pagerSelectedState
    val selectedImageList: StateFlow<List<String>> get() = _selectedImageList
    val startViewPagerIndex: SharedFlow<Int> get() = _startViewPagerIndex
    val selectErrorImage: SharedFlow<Boolean> get() = _selectErrorImage
    // 그리드 앨범 리스트 어뎁터
    val albumListAdapter = AlbumListAdapter(
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        setScrollFlag = { setScrollFlag() },
        sendErrorToast = { viewModelScope.launch { _selectErrorImage.emit(true) } },
        showViewPager = { uri -> showViewPager(uri) }
    ) { uri, state ->
        setSelectedState(uri, state)
    }
    // 뷰 페이저 앨범 리스트 어뎁터
    val albumPagerAdapter = AlbumPagerAdapter(
        sendErrorToast = { viewModelScope.launch { _selectErrorImage.emit(true) } }
    ) { uri ->
        setSelectedState(uri, findSelectedImageIndex(uri) == -1)
    }
    // 선택 사진 리스트 어뎁터
    val selectedPictureAdapter = SelectedPictureListAdapter(
        deleteSelectedImage = { setSelectedState(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        swapComplete = { swapComplete() }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }
    // 앨범 전환 시 리스트를 탑으로 올리기 위한 플래그
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
                allImages[albumName]?.let {
                    imageList.emit(it)
                    albumListAdapter.submitList(imageList.value.toMutableList())
                    currentAlbum.emit(albumName)
                    totalPictureCount = it.size
                    // 페이저 갱신
                    albumPagerAdapter.submitList(it.toMutableList())
                }
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

    fun setAlbumViewMode(state: AlbumViewState) {
        // 보기 모드를 전환하기 전에 변경 사항을 반영해준다
        if (state == AlbumViewState.GRID) {
            albumListAdapter.notifyDataSetChanged()
        }
        viewModelScope.launch {
            _albumViewMode.emit(state)
        }
    }

    fun findSelectedImageIndex(uri: String) = _selectedImageList.value.indexOf(uri)

    fun getCurrentPositionString(position: Int) = "$position / $totalPictureCount"

    // 앨범 뷰 페이저
    private fun showViewPager(uri: String) {
        allImages[currentAlbum.value]?.let { album ->
            album.indexOf(uri).let {
                if (it != -1) {
                    viewModelScope.launch {
                        _startViewPagerIndex.emit(it)
                    }
                    // 뷰 페이저 띄우기
                    setAlbumViewMode(AlbumViewState.PAGER)
                }
            }
        }
    }

    private fun swapComplete() {
        setAlbumList()
    }

    private fun setSelectedImageList() {
        selectedPictureAdapter.submitList(_selectedImageList.value.toMutableList())
        setAlbumList()
        viewModelScope.launch {
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
        selectedPictureAdapter.submitList(_selectedImageList.value.toMutableList())
    }

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
        // 현재 보기 모드가 페이저라면 선택 상태를 변경해준다.
        if (albumViewMode.value == AlbumViewState.PAGER) {
            viewModelScope.launch {
                _pagerSelectedState.emit(state)
            }
        }
        setSelectedImageList()
    }
}
