package com.fakedevelopers.bidderbidder.ui.register.acceptTerms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.RegistrationTermRepository
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class AcceptTermsViewModel @Inject constructor(
    private val repository: RegistrationTermRepository
) : ViewModel() {
    private val _termListEvent = MutableEventFlow<Response<TermListDto>>()
    private val _termContentsEvent = MutableEventFlow<Response<String>>()

    val termListEvent = _termListEvent.asEventFlow()
    val termContentsEvent = _termContentsEvent.asEventFlow()

    init {
        requestTermList()
    }

    private fun requestTermList() {
        viewModelScope.launch {
            _termListEvent.emit(repository.getRegistrationTermList())
        }
    }

    fun requestTermContents(id: Long) {
        viewModelScope.launch {
            _termContentsEvent.emit(repository.getRegistrationTermContents(id))
        }
    }
}
