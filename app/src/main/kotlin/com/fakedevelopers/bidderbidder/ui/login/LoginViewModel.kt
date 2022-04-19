package com.fakedevelopers.bidderbidder.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.UserLoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: UserLoginRepository) : ViewModel() {

    val email = MutableStateFlow("")
    val passwd = MutableStateFlow("")

    private val _loginResponse = MutableSharedFlow<Response<String>>()

    val loginResponse: SharedFlow<Response<String>> = _loginResponse

    fun loginRequest() {
        viewModelScope.launch {
            _loginResponse.emit(repository.postLogin(email.value, passwd.value))
        }
    }
}
