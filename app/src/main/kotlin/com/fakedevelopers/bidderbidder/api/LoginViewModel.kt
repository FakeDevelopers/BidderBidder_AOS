package com.fakedevelopers.bidderbidder.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.UserLoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: UserLoginRepository): ViewModel() {

    val email = MutableLiveData("")
    val passwd = MutableLiveData("")

    private val _loginResponse = MutableLiveData<Response<String>>()

    val loginResponse: LiveData<Response<String>> get() = _loginResponse

    fun loginRequest() {
        viewModelScope.launch {
            _loginResponse.value = repository.postLogin(email.value!!, passwd.value!!)
        }
    }
}
