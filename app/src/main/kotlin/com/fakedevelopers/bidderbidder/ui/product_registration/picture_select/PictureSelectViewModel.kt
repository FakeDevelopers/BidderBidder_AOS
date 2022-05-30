package com.fakedevelopers.bidderbidder.ui.product_registration.picture_select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PictureSelectViewModel : ViewModel() {

    private val imageList = MutableStateFlow<List<String>>(emptyList())
    private val _selectedIndexList = mutableListOf<Int>()

    val selectedImageList = mutableListOf<String>()
    val selectedIndexList = MutableSharedFlow<List<Int>>()
    val adapter = PictureSelectAdapter { url, position, state ->
        setSelectedState(url, position, state)
    }

    fun setList(list: List<String>) {
        viewModelScope.launch {
            imageList.emit(list)
            adapter.submitList(imageList.value)
        }
    }

    private fun setSelectedState(url: String, position: Int, state: Boolean) {
        if (state) {
            _selectedIndexList.add(position)
            selectedImageList.add(url)
        } else {
            _selectedIndexList.remove(position)
            selectedImageList.remove(url)
        }
        Logger.i(selectedImageList.joinToString(" "))
        viewModelScope.launch {
            selectedIndexList.emit(_selectedIndexList.toList())
        }
    }
}
