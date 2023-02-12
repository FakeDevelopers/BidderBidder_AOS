package com.fakedevelopers.presentation.ui.productEditor.albumSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.AlbumInfo
import com.fakedevelopers.domain.usecase.GetAlbumInfoUseCase
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumSelectViewModel @Inject constructor(
    private val getAlbumInfoUseCase: GetAlbumInfoUseCase
) : ViewModel() {

    private val _albumInfoEvent = MutableEventFlow<List<AlbumInfo>>()
    val albumInfoEvent = _albumInfoEvent.asEventFlow()

    fun initAlbumInfo(allImagesTitle: String) {
        viewModelScope.launch {
            _albumInfoEvent.emit(getAlbumInfoUseCase(allImagesTitle))
        }
    }
}
