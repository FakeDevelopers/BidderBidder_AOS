package com.fakedevelopers.presentation.ui.chat.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.presentation.api.repository.ChatRepository
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
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
            repository.getStreamUserToken(id).let {
                if (it.isSuccessful) {
                    _streamUserTokenEvent.emit(it)
                } else {
                    ApiErrorHandler.printErrorMessage(it.errorBody())
                }
            }
        }
    }

    fun setToken(token: String) {
        this.token = token
    }
}
