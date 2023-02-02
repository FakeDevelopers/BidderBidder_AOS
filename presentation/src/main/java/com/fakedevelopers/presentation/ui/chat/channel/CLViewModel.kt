package com.fakedevelopers.presentation.ui.chat.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.usecase.GetStreamUserTokenUseCase
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CLViewModel @Inject constructor(
    private val getStreamUserTokenUseCase: GetStreamUserTokenUseCase
) : ViewModel() {

    private val _streamUserTokenEvent = MutableEventFlow<Result<String>>()
    val streamUserTokenEvent = _streamUserTokenEvent.asEventFlow()
    val streamUserId = MutableStateFlow("")
    var token = ""
        private set

    fun requestStreamUserToken() {
        val id = streamUserId.value.toLongOrNull() ?: return
        viewModelScope.launch {
            _streamUserTokenEvent.emit(getStreamUserTokenUseCase(id))
        }
    }

    fun setToken(token: String) {
        this.token = token
    }
}
