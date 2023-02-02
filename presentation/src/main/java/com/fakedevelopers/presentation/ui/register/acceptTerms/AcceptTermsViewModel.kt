package com.fakedevelopers.presentation.ui.register.acceptTerms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.TermListDto
import com.fakedevelopers.domain.usecase.GetRegistrationTermContentsUseCase
import com.fakedevelopers.domain.usecase.GetRegistrationTermListUseCase
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AcceptTermsViewModel @Inject constructor(
    private val getRegistrationTermContentsUseCase: GetRegistrationTermContentsUseCase,
    private val getRegistrationTermListUseCase: GetRegistrationTermListUseCase
) : ViewModel() {
    private val _termListEvent = MutableEventFlow<Result<TermListDto>>()
    private val _termContentsEvent = MutableEventFlow<Result<String>>()

    val termListEvent = _termListEvent.asEventFlow()
    val termContentsEvent = _termContentsEvent.asEventFlow()

    init {
        requestTermList()
    }

    private fun requestTermList() {
        viewModelScope.launch {
            _termListEvent.emit(getRegistrationTermListUseCase())
        }
    }

    fun requestTermContents(id: Long) {
        viewModelScope.launch {
            _termContentsEvent.emit(getRegistrationTermContentsUseCase(id))
        }
    }
}
