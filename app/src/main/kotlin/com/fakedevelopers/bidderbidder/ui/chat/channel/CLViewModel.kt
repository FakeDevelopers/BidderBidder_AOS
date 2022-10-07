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

    private val _streamUserTokenEvent = MutableEventFlow<Response<String>>()
    val streamUserTokenEvent = _streamUserTokenEvent.asEventFlow()
    val streamUserId = MutableStateFlow("")
    var token = ""
        private set

    fun requestStreamUserToken() {
        val id = streamUserId.value.toLongOrNull() ?: return
        viewModelScope.launch {
            _streamUserTokenEvent.emit(repository.getStreamUserToken(id))
        }
    }

    fun setToken(token: String) {
        this.token = token
    }
}