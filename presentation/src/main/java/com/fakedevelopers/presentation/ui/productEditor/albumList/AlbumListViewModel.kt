package com.fakedevelopers.presentation.ui.productEditor.albumList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.domain.usecase.GetDateModifiedByUriUseCase
import com.fakedevelopers.domain.usecase.GetImageObserverUseCase
import com.fakedevelopers.domain.usecase.GetImagesUseCase
import com.fakedevelopers.domain.usecase.GetValidUrisUseCase
import com.fakedevelopers.domain.usecase.IsValidUriUseCase
import com.fakedevelopers.presentation.ui.productEditor.SelectedPictureListAdapter
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.ROTATE_DEGREE
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.roundToInt

class AlbumListViewModel @AssistedInject constructor(
    isValidUriUseCase: IsValidUriUseCase,
    private val getDateModifiedFromUriUseCase: GetDateModifiedByUriUseCase,
    private val getValidUrisUseCase: GetValidUrisUseCase,
    private val getImagesUseCase: GetImagesUseCase,
    private val getImageObserverUseCase: GetImageObserverUseCase,
    @Assisted private val path: String
) : ViewModel() {
    private val _albumViewMode = MutableStateFlow(AlbumViewState.GRID)
    val albumViewMode: StateFlow<AlbumViewState> get() = _albumViewMode

    private val _event = MutableEventFlow<Event>()
    val event = _event.asEventFlow()

    private val _editButtonEnableState = MutableStateFlow(false)
    val editButtonEnableState: StateFlow<Boolean> get() = _editButtonEnableState

    private val _albumTitle = MutableStateFlow("")
    val albumTitle: StateFlow<String> get() = _albumTitle

    private val updatedImageList = hashSetOf<String>()

    private var allImages = mutableListOf<AlbumItem>()
    val selectedImageInfo = SelectedImageInfo()

    // 현재 뷰 페이저 인덱스
    var currentViewPagerIdx = 0
        private set

    val title = path.substringAfterLast('/')

    @AssistedFactory
    interface PathAssistedFactory {
        fun create(path: String): AlbumListViewModel
    }

    init {
        viewModelScope.launch {
            _albumTitle.emit(title)
            allImages = getImagesUseCase(path).toMutableList()
            sendEvent(Event.AlbumList(allImages))
            getImageObserverUseCase().collect { uri ->
                updatedImageList.add(uri)
            }
        }
    }

    // 그리드 앨범 리스트 어뎁터
    val albumListAdapter = AlbumListAdapter(
        isValidUri = { uri -> isValidUriUseCase(uri) },
        findSelectedImageIndex = { selectedImageInfo.uris.indexOf(it) },
        sendErrorToast = { sendEvent(Event.SelectErrorImage(true)) },
        showViewPager = { uri -> showViewPager(uri) }
    ) { uri, state ->
        setSelectedState(uri, state)
    }

    // 뷰 페이저 앨범 리스트 어뎁터
    val albumPagerAdapter = AlbumPagerAdapter(
        isValidUri = { uri -> isValidUriUseCase(uri) },
        sendErrorToast = { sendEvent(Event.SelectErrorImage(true)) },
        getEditedImage = { uri -> selectedImageInfo.changeBitmaps[uri] }
    ) { uri ->
        setSelectedState(uri, selectedImageInfo.uris.indexOf(uri) == -1)
    }

    // 선택 사진 리스트 어뎁터
    val selectedPictureAdapter = SelectedPictureListAdapter(
        deleteSelectedImage = {
            setSelectedState(it)
            albumListAdapter.refreshSelectedOrder()
        },
        findSelectedImageIndex = { selectedImageInfo.uris.indexOf(it) },
        swapSelectedImage = { fromPosition, toPosition -> swapSelectedImage(fromPosition, toPosition) }
    )

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

    fun rotateCurrentImage() {
        val uri = allImages[currentViewPagerIdx].uri
        // 로테이트된 비트맵이 있으면 그걸 돌림
        // 없다면 새로 추가
        selectedImageInfo.changeBitmaps[uri]?.let { bitmapInfo ->
            bitmapInfo.degree += ROTATE_DEGREE
            // 360도 돌아갔다면 변경 사항이 없는거다. bitmapInfo를 삭제한다.
            if (bitmapInfo.degree.roundToInt() == 360) {
                selectedImageInfo.changeBitmaps.remove(uri)
            }
        } ?: addEditedBitmapInfo(uri)
        // 이미지 새로고침
        albumPagerAdapter.notifyItemChanged(currentViewPagerIdx)
    }

    // 수정된 이미지 비트맵 추가
    private fun addEditedBitmapInfo(uri: String) {
        if (selectedImageInfo.uris.indexOf(uri) == -1) {
            setSelectedState(uri, true)
        }
        selectedImageInfo.changeBitmaps[uri] = BitmapInfo(ROTATE_DEGREE)
    }

    fun setCurrentViewPagerIdx(idx: Int) {
        currentViewPagerIdx = idx
        sendEvent(Event.ImageCount(selectedImageInfo.uris.indexOf(allImages[currentViewPagerIdx].uri)))
        viewModelScope.launch {
            _albumTitle.emit("${idx + 1} / ${allImages.size}")
        }
    }

    fun setAlbumViewMode(state: AlbumViewState) {
        // 보기 모드를 전환하기 전에 변경 사항을 반영해준다
        if (state == AlbumViewState.GRID) {
            albumListAdapter.refreshAll()
            viewModelScope.launch {
                _albumTitle.emit(title)
            }
        }
        viewModelScope.launch {
            _albumViewMode.emit(state)
        }
    }

    private fun setSelectedImage(list: List<String>) {
        selectedImageInfo.uris.filter { !list.contains(it) }.forEach { uri ->
            removeImage(uri)
            selectedImageInfo.changeBitmaps.remove(uri)
        }
        viewModelScope.launch {
            selectedPictureAdapter.submitList(list.toMutableList())
            if (list.isNotEmpty() && !list.contains(selectedImageInfo.uris[0])) {
                selectedPictureAdapter.notifyItemChanged(selectedImageInfo.uris.indexOf(list[0]))
            }
            setAdapterList()
        }
        selectedImageInfo.uris = list.toMutableList()
        albumListAdapter.refreshSelectedOrder()
    }

    private fun setSelectedState(uri: String, state: Boolean = false) {
        if (state) {
            selectedImageInfo.uris.add(uri)
        } else {
            val idx = selectedImageInfo.uris.indexOf(uri)
            selectedImageInfo.uris.removeAt(idx)
            // 수정된 내용(BitmapInfo)도 같이 삭제
            if (selectedImageInfo.changeBitmaps.remove(uri) != null) {
                // 페이저에 보이는 이미지 원상 복구
                albumPagerAdapter.notifyItemChanged(allImages.indexOfFirst { it.uri == uri })
            }
            // 첫번째 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
            if (selectedImageInfo.uris.isNotEmpty() && idx == 0) {
                selectedPictureAdapter.notifyItemChanged(1)
            }
        }
        // 선택 상태를 변경해준다.
        viewModelScope.launch {
            if (state) {
                sendEvent(Event.ImageCount(selectedImageInfo.uris.lastIndex))
            } else {
                sendEvent(Event.ImageCount(-1))
            }
        }
        // 선택 이미지가 남아 있으면 편집 버튼 활성화
        viewModelScope.launch {
            _editButtonEnableState.emit(selectedImageInfo.uris.isNotEmpty())
        }
        setSelectedImageList()
    }

    // 편집 버튼 클릭
    fun onEditButtonClick() {
        showViewPager(selectedImageInfo.uris.last())
    }

    fun updateAlbumList() {
        val updatedAlbumItems = updatedImageList.mapNotNull { getDateModifiedFromUriUseCase(it) }
        updatedImageList.forEach { uri ->
            removeImage(uri)
        }
        // 앨범 리스트 갱신
        updatedAlbumItems.forEach { item ->
            allImages.add(AlbumItem(item.uri, item.modified))
        }
        // 수정된 날짜 기준으로 소팅
        if (updatedAlbumItems.isNotEmpty()) {
            allImages.sortByDescending { it.modified }
        }
        updatedImageList.clear()
        setAdapterList()
    }

    fun checkSelectedImages(idx: Int? = null) {
        // 선택 이미지 리스트가 존재한다면 유효한지 검사
        if (selectedImageInfo.uris.isNotEmpty()) {
            // 유효한 선택 이미지 리스트로 갱신
            setSelectedImage(getValidUrisUseCase(selectedImageInfo.uris))
            if (albumViewMode.value == AlbumViewState.PAGER && idx != null) {
                sendEvent(Event.ImageCount(selectedImageInfo.uris.indexOf(allImages[idx].uri)))
            }
        }
    }

    private fun removeImage(uri: String) {
        val targetImage = allImages.find { it.uri == uri } ?: return
        allImages.remove(targetImage)
    }

    private fun setAdapterList() {
        val currentList = mutableListOf<AlbumItem>().apply { addAll(allImages) }
        albumListAdapter.submitList(currentList)
        albumPagerAdapter.submitList(currentList)
    }

    // 앨범 뷰 페이저
    private fun showViewPager(uri: String) {
        val idx = allImages.indexOfFirst { it.uri == uri }
        if (idx != -1) {
            sendEvent(Event.StartViewPagerIndex(idx))
            setAlbumViewMode(AlbumViewState.PAGER)
        }
    }

    private fun setSelectedImageList() {
        selectedPictureAdapter.submitList(selectedImageInfo.uris.toMutableList())
        sendEvent(Event.OnListChange(selectedImageInfo.uris.size))
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        Collections.swap(selectedImageInfo.uris, fromPosition, toPosition)
        selectedPictureAdapter.submitList(selectedImageInfo.uris.toMutableList())
        albumListAdapter.refreshSelectedOrder()
    }

    private fun sendEvent(event: Event) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    sealed class Event {
        data class OnListChange(val count: Int) : Event()
        data class SelectErrorImage(val state: Boolean) : Event()
        data class StartViewPagerIndex(val idx: Int) : Event()
        data class ImageCount(val count: Int) : Event()
        data class AlbumList(val albums: List<AlbumItem>) : Event()
    }
}
