package com.fakedevelopers.presentation.ui.loginType

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.LoginInfo
import com.fakedevelopers.domain.usecase.LoginWithGoogleUseCase
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginTypeViewModel @Inject constructor(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _loginWithGoogleEvent = MutableEventFlow<Result<LoginInfo>>()
    val loginWithGoogleEvent = _loginWithGoogleEvent.asEventFlow()

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginWithGoogleEvent.emit(loginWithGoogleUseCase(idToken))
        }
    }
}
