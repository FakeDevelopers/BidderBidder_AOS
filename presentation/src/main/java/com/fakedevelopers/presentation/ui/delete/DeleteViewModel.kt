package com.fakedevelopers.presentation.ui.delete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.usecase.CheckUserIsSameUseCase
import com.fakedevelopers.domain.usecase.DeleteProductUseCase
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteViewModel @Inject constructor(
    private val checkUserIsSameUseCase: CheckUserIsSameUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    private val _checkEvent = MutableEventFlow<Result<Boolean>>()
    val checkEvent = _checkEvent.asEventFlow()

    private val _deleteEvent = MutableEventFlow<Result<Boolean>>()
    val deleteEvent = _deleteEvent.asEventFlow()

    val productId = MutableStateFlow("")

    fun requestCheck() {
        val id = productId.value.toLongOrNull() ?: return
        viewModelScope.launch {
            _deleteEvent.emit(checkUserIsSameUseCase(id))
        }
    }

    fun requestDelete() {
        val id = productId.value.toLongOrNull() ?: return
        viewModelScope.launch {
            _deleteEvent.emit(deleteProductUseCase(id))
        }
    }
}
