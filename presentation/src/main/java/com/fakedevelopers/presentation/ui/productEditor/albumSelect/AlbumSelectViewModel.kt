package com.fakedevelopers.presentation.ui.productEditor.albumSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.domain.usecase.GetImagesUseCase
import com.fakedevelopers.presentation.model.AlbumInfo
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AlbumSelectViewModel @Inject constructor(
    private val getImagesUseCase: GetImagesUseCase
) : ViewModel() {

    private val _albumInfoEvent = MutableEventFlow<List<AlbumInfo>>()
    val albumInfoEvent = _albumInfoEvent.asEventFlow()

    init {
        viewModelScope.launch {
            initAlbumInfo(getImagesUseCase())
        }
    }

    private suspend fun initAlbumInfo(albumItems: List<AlbumItem>) {
        val albumInfo = mutableListOf<AlbumInfo>()
        albumInfo.add(
            AlbumInfo(
                path = "",
                firstImage = albumItems[0].uri,
                name = "최근 항목",
                count = albumItems.size
            )
        )
        val countMap = mutableMapOf<String, Int>()
        albumItems.forEach { albumItem ->
            countMap[albumItem.path] = (countMap[albumItem.path] ?: 0) + 1
        }
        countMap.keys.forEach { path ->
            albumInfo.add(
                AlbumInfo(
                    path = path,
                    firstImage = albumItems.find { it.path == path }?.uri ?: "",
                    name = path.substringAfterLast('/'),
                    count = countMap[path] ?: 0
                )
            )
        }
        _albumInfoEvent.emit(albumInfo)
    }
}
