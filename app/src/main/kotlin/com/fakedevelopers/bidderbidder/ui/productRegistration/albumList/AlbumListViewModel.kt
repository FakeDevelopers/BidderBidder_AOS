package com.fakedevelopers.bidderbidder.ui.productRegistration.albumList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.productRegistration.SelectedPictureListAdapter
import com.fakedevelopers.bidderbidder.ui.productRegistration.albumList.AlbumListFragment.Companion.ADD_IMAGE
import com.fakedevelopers.bidderbidder.ui.productRegistration.albumList.AlbumListFragment.Companion.ALL_PICTURES
import com.fakedevelopers.bidderbidder.ui.productRegistration.albumList.AlbumListFragment.Companion.MODIFY_IMAGE
import com.fakedevelopers.bidderbidder.ui.productRegistration.albumList.AlbumListFragment.Companion.REMOVE_IMAGE
import com.fakedevelopers.bidderbidder.ui.util.ContentResolverUtil
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class AlbumListViewModel @Inject constructor(
    contentResolverUtil: ContentResolverUtil
) : ViewModel() {

    private val currentAlbum = MutableStateFlow("")
    private val _albumViewMode = MutableStateFlow(AlbumViewState.GRID)
    private val _onListChange = MutableEventFlow<Boolean>()
    private val _pagerSelectedState = MutableEventFlow<Boolean>()
    private val _selectErrorImage = MutableEventFlow<Boolean>()
    private val _startViewPagerIndex = MutableEventFlow<Int>()
    private val _editButtonEnableState = MutableStateFlow(false)
    private val _addedImageList = hashSetOf<String>()
    private val removedImageList = hashSetOf<String>()
    private var totalPictureCount = 0
    private lateinit var allImages: Map<String, MutableList<AlbumItem>>

    val albumViewMode: StateFlow<AlbumViewState> get() = _albumViewMode
    val onListChange = _onListChange.asEventFlow()
    val pagerSelectedState = _pagerSelectedState.asEventFlow()
    val startViewPagerIndex = _startViewPagerIndex.asEventFlow()
    val editButtonEnableState: StateFlow<Boolean> get() = _editButtonEnableState
    val selectErrorImage = _selectErrorImage.asEventFlow()
    val addedImageList: Set<String> get() = _addedImageList
    val selectedImageInfo = SelectedImageInfo()

    // 현재 뷰 페이저 인덱스
    var currentViewPagerIdx = 0
        private set

    // 그리드 앨범 리스트 어뎁터
    val albumListAdapter = AlbumListAdapter(
        contentResolverUtil,
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        sendErrorToast = { viewModelScope.launch { _selectErrorImage.emit(true) } },
        showViewPager = { uri -> showViewPager(uri) }
    ) { uri, state ->
        setSelectedState(uri, state)
    }

    // 뷰 페이저 앨범 리스트 어뎁터
    val albumPagerAdapter = AlbumPagerAdapter(
        contentResolverUtil,
        sendErrorToast = { viewModelScope.launch { _selectErrorImage.emit(true) } },
        getEditedImage = { uri -> getEditedBitmapInfo(uri) }
    ) { uri ->
        setSelectedState(uri, findSelectedImageIndex(uri) == -1)
    }

    // 선택 사진 리스트 어뎁터
    val selectedPictureAdapter = SelectedPictureListAdapter(
        deleteSelectedImage = { setSelectedState(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) },
        swapComplete = { albumPagerAdapter.notifyDataSetChanged() }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }

    // 앨범 전환 시 리스트를 탑으로 올리기 위한 플래그
    var scrollToTopFlag = false

    fun initSelectedImageList(selectedImageInfo: SelectedImageInfo) {
        this.selectedImageInfo.apply {
            uris = selectedImageInfo.uris
            changeBitmaps.putAll(selectedImageInfo.changeBitmaps)
        }
        viewModelScope.launch {
            // 선택 이미지를 세팅 해주고
            setSelectedImageList()
            // 편집 버튼 활성화
            _editButtonEnableState.emit(true)
        }
    }

    fun initAlbumInfo(map: Map<String, MutableList<AlbumItem>>) {
        allImages = map
    }

    // 수정된 이미지 비트맵 추가
    fun addBitmapInfo(uri: String, bitmapInfo: BitmapInfo) {
        selectedImageInfo.changeBitmaps[uri] = bitmapInfo
    }

    // 수정된 이미지 비트맵 삭제
    fun removeBitmapInfo(uri: String) {
        selectedImageInfo.changeBitmaps.remove(uri)
    }

    fun setScrollFlag() {
        scrollToTopFlag = !scrollToTopFlag
    }

    fun setCurrentViewPagerIdx(idx: Int) {
        currentViewPagerIdx = idx
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

    fun setSelectedImage(list: List<String>) {
        selectedImageInfo.uris.filter { !list.contains(it) }.forEach { uri ->
            removeInvalidImage(uri)
        }
        viewModelScope.launch {
            selectedPictureAdapter.submitList(list.toMutableList())
            if (list.isNotEmpty() && !list.contains(selectedImageInfo.uris[0])) {
                selectedPictureAdapter.notifyItemChanged(findSelectedImageIndex(list[0]))
            }
            albumListAdapter.notifyDataSetChanged()
            setAdapterList()
        }
        selectedImageInfo.uris = list.toMutableList()
    }

    fun setSelectedState(uri: String, state: Boolean = false) {
        if (state) {
            selectedImageInfo.uris.add(uri)
        } else {
            val idx = findSelectedImageIndex(uri)
            selectedImageInfo.run {
                uris.removeAt(idx)
                // 수정된 내용(BitmapInfo)도 같이 삭제
                if (changeBitmaps.contains(uri)) {
                    changeBitmaps.remove(uri)
                    // 페이저에 보이는 이미지 원상 복구
                    allImages[currentAlbum.value]?.let { list ->
                        albumPagerAdapter.notifyItemChanged(list.indexOfFirst { it.uri == uri })
                    }
                }
                // 첫번째 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
                if (uris.isNotEmpty() && idx == 0) {
                    selectedPictureAdapter.notifyItemChanged(1)
                }
            }
        }
        // 현재 보기 모드가 페이저라면 선택 상태를 변경해준다.
        viewModelScope.launch {
            _pagerSelectedState.emit(state)
        }
        // 선택 이미지가 남아 있으면 편집 버튼 활성화
        viewModelScope.launch {
            _editButtonEnableState.emit(selectedImageInfo.uris.isNotEmpty())
        }
        setSelectedImageList()
    }

    fun findSelectedImageIndex(uri: String) = selectedImageInfo.uris.indexOf(uri)

    fun getCurrentPositionString(position: Int) = "$position / $totalPictureCount"

    fun getCurrentUri() = allImages[currentAlbum.value]?.get(currentViewPagerIdx)?.uri ?: ""

    fun getPictureUri(albumName: String = currentAlbum.value, position: Int) =
        allImages[albumName]?.get(position)?.uri ?: ""

    // 수정된 비트맵 가져오기
    fun getEditedBitmapInfo(uri: String) =
        selectedImageInfo.changeBitmaps[uri]

    fun isAlbumListChanged() =
        albumListAdapter.currentList[0] == allImages[currentAlbum.value]?.get(0)

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

    // 편집 버튼 클릭
    fun onEditButtonClick() {
        showViewPager(selectedImageInfo.uris.last())
    }

    fun updateAlbumList(
        validAddedImageList: List<Triple<String, String, Long>>,
        albumName: String = currentAlbum.value
    ) {
        if (removedImageList.isNotEmpty()) {
            // 앨범 리스트 갱신
            for (uri in removedImageList) {
                removeInvalidImage(uri)
            }
            removedImageList.clear()
        }
        if (validAddedImageList.isNotEmpty()) {
            // 앨범 리스트 갱신
            for ((uri, rel, date) in validAddedImageList) {
                val albumItem = AlbumItem(uri, date)
                allImages[ALL_PICTURES]?.add(albumItem)
                allImages[rel]?.add(albumItem)
            }
            // 수정된 날짜 기준으로 소팅
            for (key in allImages.keys) {
                allImages[key]?.sortByDescending { it.modifiedTime }
            }
            _addedImageList.clear()
        }
        setAdapterList(albumName)
    }

    // 유효하지 않은 이미지 제거
    private fun removeInvalidImage(uri: String) {
        val invalidImage = allImages[ALL_PICTURES]?.find { it.uri == uri }
        allImages[ALL_PICTURES]?.remove(invalidImage)
        for (key in allImages.keys) {
            if (allImages[key]?.remove(invalidImage) == true) {
                break
            }
        }
    }

    private fun setAdapterList(albumName: String = currentAlbum.value) {
        allImages[albumName]?.let { list ->
            val currentList = mutableListOf<AlbumItem>().apply { addAll(list) }
            albumListAdapter.submitList(currentList)
            albumPagerAdapter.submitList(currentList)
            totalPictureCount = list.size
        }
        albumListAdapter.notifyDataSetChanged()
        selectedPictureAdapter.notifyDataSetChanged()
        if (albumName != currentAlbum.value) {
            viewModelScope.launch {
                currentAlbum.emit(albumName)
                // 앨범을 바꿀 때 최상위 스크롤을 해주는 플래그를 true로 바꿔준다.
                setScrollFlag()
            }
        }
    }

    // 앨범 뷰 페이저
    private fun showViewPager(uri: String) {
        allImages[currentAlbum.value]?.let { album ->
            val idx = album.indexOf(album.find { it.uri == uri })
            if (idx != -1) {
                viewModelScope.launch {
                    _startViewPagerIndex.emit(idx)
                }
                setAlbumViewMode(AlbumViewState.PAGER)
            }
        }
    }

    private fun setSelectedImageList() {
        selectedPictureAdapter.submitList(mutableListOf<String>().apply { addAll(selectedImageInfo.uris) })
        albumListAdapter.notifyDataSetChanged()
        viewModelScope.launch {
            _onListChange.emit(true)
        }
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        Collections.swap(selectedImageInfo.uris, fromPosition, toPosition)
        selectedPictureAdapter.submitList(mutableListOf<String>().apply { addAll(selectedImageInfo.uris) })
        albumListAdapter.notifyDataSetChanged()
        albumPagerAdapter.notifyDataSetChanged()
    }
}
