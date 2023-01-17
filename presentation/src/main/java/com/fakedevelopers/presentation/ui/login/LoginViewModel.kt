package com.fakedevelopers.presentation.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.usecase.LoginWithEmailUseCase
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase
) : ViewModel() {

    val email = MutableStateFlow("")
    val passwd = MutableStateFlow("")

    private val _loginEvent = MutableEventFlow<Result<String>>()
    val loginEvent = _loginEvent.asEventFlow()

    fun loginRequest() {
        viewModelScope.launch {
            _loginEvent.emit(loginWithEmailUseCase(email.value, passwd.value))
        }
    }
}
