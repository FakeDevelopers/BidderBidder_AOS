package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.ui.product_registration.SelectedPictureListAdapter
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.ADD_IMAGE
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.ALL_PICTURES
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.MODIFY_IMAGE
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumListFragment.Companion.REMOVE_IMAGE
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections

// -아래에 헤더가 존재-
// -편집 기능 버튼-
// 비활성화 -> 아무 사진도 선택 안됨 || 페이저 모드
// 활성화 -> 사진이 하나라도 선택 됨 && 그리드 모드
// 클릭 -> selectedImageList의 top 요소를 페이저로 보여줌
// -회전 기능-
// 비활성화 -> 그리드 모드
// 활성화 -> 페이저 모드
// 90 -> 180 -> 270 -> 0(0이면 회전 기능을 사용 안한 것과 같은 효과)
// 회전 적용 시 페이저에선 회전한 사진으로 보임, 그리드에선 원본 사진으로 보임
// 선택 취소 시 회전 효과도 사라짐

class AlbumListViewModel : ViewModel() {

    private val currentAlbum = MutableStateFlow("")
    private val _albumViewMode = MutableStateFlow(AlbumViewState.GRID)
    private val _onListChange = MutableEventFlow<Boolean>()
    private val _pagerSelectedState = MutableEventFlow<Boolean>()
    private val _selectErrorImage = MutableEventFlow<Boolean>()
    private val _startViewPagerIndex = MutableEventFlow<Int>()
    private val _addedImageList = hashSetOf<String>()
    private val removedImageList = hashSetOf<String>()
    private var totalPictureCount = 0
    private lateinit var allImages: Map<String, MutableList<Pair<String, Long>>>

    val albumViewMode: StateFlow<AlbumViewState> get() = _albumViewMode
    val onListChange = _onListChange.asEventFlow()
    val pagerSelectedState = _pagerSelectedState.asEventFlow()
    val startViewPagerIndex = _startViewPagerIndex.asEventFlow()
    val selectErrorImage = _selectErrorImage.asEventFlow()
    val addedImageList: Set<String> get() = _addedImageList
    val selectedImageDto = SelectedImageDto()

    // 그리드 앨범 리스트 어뎁터
    val albumListAdapter = AlbumListAdapter(
        findSelectedImageIndex = { findSelectedImageIndex(it) },
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
        swapComplete = { albumPagerAdapter.notifyDataSetChanged() }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }

    // 앨범 전환 시 리스트를 탑으로 올리기 위한 플래그
    var scrollToTopFlag = false

    fun initSelectedImageList(list: List<String>) {
        selectedImageDto.uris = list.toMutableList()
        viewModelScope.launch {
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
        // 보기 모드를 전환하기 전에 변경 사항을 반영해준다
        if (state == AlbumViewState.GRID) {
            albumListAdapter.notifyDataSetChanged()
        }
        viewModelScope.launch {
            _albumViewMode.emit(state)
        }
    }

    fun setSelectedImage(list: List<String>) {
        val invalidList = selectedImageDto.uris.filter { !list.contains(it) }
        for (uri in invalidList) {
            removeInvalidImage(uri)
        }
        viewModelScope.launch {
            selectedPictureAdapter.submitList(list.toMutableList())
            if (list.isNotEmpty() && !list.contains(selectedImageDto.uris[0])) {
                selectedPictureAdapter.notifyItemChanged(findSelectedImageIndex(list[0]))
            }
            albumListAdapter.notifyDataSetChanged()
            setAdapterList()
        }
        selectedImageDto.uris = list.toMutableList()
    }

    fun findSelectedImageIndex(uri: String) = selectedImageDto.uris.indexOf(uri)

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
            // 앨범 리스트 갱신
            for (uri in removedImageList) {
                removeInvalidImage(uri)
            }
            removedImageList.clear()
        }
        if (validAddedImageList.isNotEmpty()) {
            // 앨범 리스트 갱신
            for ((uri, rel, date) in validAddedImageList) {
                allImages[ALL_PICTURES]?.add(uri to date)
                allImages[rel]?.add(uri to date)
            }
            // 수정된 날짜 기준으로 소팅
            for (key in allImages.keys) {
                allImages[key]?.sortByDescending { it.second }
            }
            _addedImageList.clear()
        }
        setAdapterList(albumName)
    }

    // 유효하지 않은 이미지 제거
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
                // 앨범을 바꿀 때 최상위 스크롤을 해주는 플래그를 true로 바꿔준다.
                setScrollFlag()
            }
        }
    }

    // 앨범 뷰 페이저
    private fun showViewPager(uri: String) {
        allImages[currentAlbum.value]?.let { album ->
            album.indexOf(album.find { it.first == uri }).let {
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

    private fun setSelectedImageList() {
        selectedPictureAdapter.submitList(mutableListOf<String>().apply { addAll(selectedImageDto.uris) })
        albumListAdapter.notifyDataSetChanged()
        viewModelScope.launch {
            _onListChange.emit(true)
        }
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(selectedImageDto.uris, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(selectedImageDto.uris, i, i - 1)
            }
        }
        selectedPictureAdapter.submitList(mutableListOf<String>().apply { addAll(selectedImageDto.uris) })
    }

    private fun setSelectedState(uri: String, state: Boolean = false) {
        if (state) {
            selectedImageDto.uris.add(uri)
        } else {
            val idx = findSelectedImageIndex(uri)
            selectedImageDto.uris.removeAt(idx)
            // 첫번째 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
            if (selectedImageDto.uris.isNotEmpty() && idx == 0) {
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
