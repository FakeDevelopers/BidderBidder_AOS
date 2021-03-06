package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.product_registration.SelectedPictureListAdapter
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.ADD_IMAGE
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.ALL_PICTURES
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.MODIFY_IMAGE
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.REMOVE_IMAGE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections

class AlbumListViewModel : ViewModel() {

    private val currentAlbum = MutableStateFlow("")
    private val _albumViewMode = MutableStateFlow(AlbumViewState.GRID)
    private val _onListChange = MutableSharedFlow<Boolean>()
    private val _pagerSelectedState = MutableSharedFlow<Boolean>()
    private val _selectedImageList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private val _selectErrorImage = MutableSharedFlow<Boolean>()
    private val _startViewPagerIndex = MutableSharedFlow<Int>()
    private val _addedImageList = hashSetOf<String>()
    private val removedImageList = hashSetOf<String>()
    private var totalPictureCount = 0
    private lateinit var allImages: Map<String, MutableList<Pair<String, Long>>>

    val albumViewMode: StateFlow<AlbumViewState> get() = _albumViewMode
    val onListChange: SharedFlow<Boolean> get() = _onListChange
    val pagerSelectedState: SharedFlow<Boolean> get() = _pagerSelectedState
    val selectedImageList: StateFlow<List<String>> get() = _selectedImageList
    val startViewPagerIndex: SharedFlow<Int> get() = _startViewPagerIndex
    val selectErrorImage: SharedFlow<Boolean> get() = _selectErrorImage
    val addedImageList: Set<String> get() = _addedImageList

    // ????????? ?????? ????????? ?????????
    val albumListAdapter = AlbumListAdapter(
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        sendErrorToast = { viewModelScope.launch { _selectErrorImage.emit(true) } },
        showViewPager = { uri -> showViewPager(uri) }
    ) { uri, state ->
        setSelectedState(uri, state)
    }

    // ??? ????????? ?????? ????????? ?????????
    val albumPagerAdapter = AlbumPagerAdapter(
        sendErrorToast = { viewModelScope.launch { _selectErrorImage.emit(true) } }
    ) { uri ->
        setSelectedState(uri, findSelectedImageIndex(uri) == -1)
    }

    // ?????? ?????? ????????? ?????????
    val selectedPictureAdapter = SelectedPictureListAdapter(
        deleteSelectedImage = { setSelectedState(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        swapComplete = { albumPagerAdapter.notifyDataSetChanged() }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }

    // ?????? ?????? ??? ???????????? ????????? ????????? ?????? ?????????
    var scrollToTopFlag = false

    fun initSelectedImageList(list: List<String>) {
        viewModelScope.launch {
            _selectedImageList.emit(list.toMutableList())
            setSelectedImageList()
        }
    }

    fun initAlbumInfo(map: Map<String, MutableList<Pair<String, Long>>>) {
        allImages = map
    }

    fun setScrollFlag() {
        scrollToTopFlag = !scrollToTopFlag
    }

    fun setAlbumViewMode(state: AlbumViewState) {
        // ?????? ????????? ???????????? ?????? ?????? ????????? ???????????????
        if (state == AlbumViewState.GRID) {
            albumListAdapter.notifyDataSetChanged()
        }
        viewModelScope.launch {
            _albumViewMode.emit(state)
        }
    }

    fun setSelectedImage(list: List<String>) {
        val invalidList = selectedImageList.value.filter { !list.contains(it) }
        for (uri in invalidList) {
            removeInvalidImage(uri)
        }
        viewModelScope.launch {
            selectedPictureAdapter.submitList(list.toMutableList())
            if (list.isNotEmpty() && !list.contains(selectedImageList.value[0])) {
                selectedPictureAdapter.notifyItemChanged(selectedImageList.value.indexOf(list[0]))
            }
            albumListAdapter.notifyDataSetChanged()
            setAdapterList()
            _selectedImageList.emit(list.toMutableList())
        }
    }

    fun findSelectedImageIndex(uri: String) = _selectedImageList.value.indexOf(uri)

    fun getCurrentPositionString(position: Int) = "$position / $totalPictureCount"

    fun getPictureUri(albumName: String = currentAlbum.value, position: Int) =
        allImages[albumName]?.get(position)?.first ?: ""

    fun isAlbumListChanged() =
        albumListAdapter.currentList[0] == allImages[currentAlbum.value]?.let { it[0] }

    fun onAlbumListChanged(uri: String, type: Int) {
        when (type) {
            ADD_IMAGE -> _addedImageList.add(uri)
            REMOVE_IMAGE -> removedImageList.add(uri)
            MODIFY_IMAGE -> {
                _addedImageList.add(uri)
                removedImageList.add(uri)
            }
        }
    }

    fun updateAlbumList(
        validAddedImageList: List<Triple<String, String, Long>>,
        albumName: String = currentAlbum.value
    ) {
        if (removedImageList.isNotEmpty()) {
            // ?????? ????????? ??????
            for (uri in removedImageList) {
                removeInvalidImage(uri)
            }
            removedImageList.clear()
        }
        if (validAddedImageList.isNotEmpty()) {
            // ?????? ????????? ??????
            for ((uri, rel, date) in validAddedImageList) {
                allImages[ALL_PICTURES]?.add(uri to date)
                allImages[rel]?.add(uri to date)
            }
            // ????????? ?????? ???????????? ??????
            for (key in allImages.keys) {
                allImages[key]?.sortByDescending { it.second }
            }
            _addedImageList.clear()
        }
        setAdapterList(albumName)
    }

    // ???????????? ?????? ????????? ??????
    private fun removeInvalidImage(uri: String) {
        val invalidImage = allImages[ALL_PICTURES]?.find { it.first == uri }
        allImages[ALL_PICTURES]?.remove(invalidImage)
        for (key in allImages.keys) {
            if (allImages[key]?.remove(invalidImage) == true) {
                break
            }
        }
    }

    private fun setAdapterList(albumName: String = currentAlbum.value) {
        allImages[albumName]?.let { list ->
            val currentList = mutableListOf<Pair<String, Long>>().apply { addAll(list) }
            albumListAdapter.submitList(currentList)
            albumPagerAdapter.submitList(currentList)
            totalPictureCount = list.size
        }
        albumListAdapter.notifyDataSetChanged()
        selectedPictureAdapter.notifyDataSetChanged()
        if (albumName != currentAlbum.value) {
            viewModelScope.launch {
                currentAlbum.emit(albumName)
                // ????????? ?????? ??? ????????? ???????????? ????????? ???????????? true??? ????????????.
                setScrollFlag()
            }
        }
    }

    // ?????? ??? ?????????
    private fun showViewPager(uri: String) {
        allImages[currentAlbum.value]?.let { album ->
            album.indexOf(album.find { it.first == uri }).let {
                if (it != -1) {
                    viewModelScope.launch {
                        _startViewPagerIndex.emit(it)
                    }
                    // ??? ????????? ?????????
                    setAlbumViewMode(AlbumViewState.PAGER)
                }
            }
        }
    }

    private fun setSelectedImageList() {
        selectedPictureAdapter.submitList(mutableListOf<String>().apply { addAll(_selectedImageList.value) })
        albumListAdapter.notifyDataSetChanged()
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
        selectedPictureAdapter.submitList(mutableListOf<String>().apply { addAll(_selectedImageList.value) })
    }

    private fun setSelectedState(uri: String, state: Boolean = false) {
        if (state) {
            _selectedImageList.value.add(uri)
        } else {
            val idx = _selectedImageList.value.indexOf(uri)
            _selectedImageList.value.removeAt(idx)
            // ????????? ????????? ?????? ????????? ?????? ???????????? ???????????? ?????????
            if (_selectedImageList.value.isNotEmpty() && idx == 0) {
                selectedPictureAdapter.notifyItemChanged(1)
            }
        }
        // ?????? ?????? ????????? ??????????????? ?????? ????????? ???????????????.
        if (albumViewMode.value == AlbumViewState.PAGER) {
            viewModelScope.launch {
                _pagerSelectedState.emit(state)
            }
        }
        setSelectedImageList()
    }
}
