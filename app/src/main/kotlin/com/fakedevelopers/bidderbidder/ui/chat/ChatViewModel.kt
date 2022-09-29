package com.fakedevelopers.bidderbidder.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ChatRepository
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

@HiltViewModel
class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _getStreamUserTokenEvent = MutableEventFlow<Response<String>>()
    val getStreamUserTokenEvent = _getStreamUserTokenEvent.asEventFlow()
    val streamUserId = MutableStateFlow("")

    fun requestStreamUserToken() {
        viewModelScope.launch {
            _getStreamUserTokenEvent.emit(repository.getStreamUserId(streamUserId.value.toInt()))
        }
    }
}
