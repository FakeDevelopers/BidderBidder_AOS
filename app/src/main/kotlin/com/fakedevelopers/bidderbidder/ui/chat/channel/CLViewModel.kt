package com.fakedevelopers.bidderbidder.ui.chat.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ChatRepository
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class CLViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _getStreamUserTokenEvent = MutableEventFlow<Response<String>>()
    val getStreamUserTokenEvent = _getStreamUserTokenEvent.asEventFlow()
    val streamUserId = MutableStateFlow("")
    var token = ""
        private set

    fun requestStreamUserToken() {
        viewModelScope.launch {
            _getStreamUserTokenEvent.emit(repository.getStreamUserToken(streamUserId.value.toLong()))
        }
    }

    fun setToken(token: String) {
        this.token = token
    }
}
