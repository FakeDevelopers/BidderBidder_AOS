package com.fakedevelopers.bidderbidder.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.UserLoginRepository
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
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

    val loginResponse: SharedFlow<Response<String>> get() = _loginResponse

    fun loginRequest() {
        viewModelScope.launch {
            repository.postLogin(email.value, passwd.value).let {
                if (it.isSuccessful) {
                    _loginResponse.emit(it)
                } else {
                    ApiErrorHandler.handleError(it.errorBody())
                }
            }
        }
    }
}
