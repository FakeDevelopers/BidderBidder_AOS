package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableStateFlow("")
    val id = MutableStateFlow("")
    val password = MutableStateFlow("")
    val passwordAgain = MutableStateFlow("")

    private val confirmedId = MutableStateFlow("")
    private val _birth = MutableStateFlow("")
    private val _birthCheck = MutableStateFlow(false)
    private val _idCheck = MutableStateFlow(false)
    private val _passwordCheck = MutableStateFlow(false)

    val birth: StateFlow<String> = _birth
    val birthCheck: StateFlow<Boolean> = _birthCheck
    val idCheck: StateFlow<Boolean> = _idCheck
    val passwordCheck: StateFlow<Boolean> = _passwordCheck

    fun setBirth(userBirth: String) {
        _birth.value = userBirth
        viewModelScope.launch {
            _birthCheck.emit(true)
        }
    }

    fun idDuplicationCheck() {
        // 여기서 id.value와 중복되는 id가 있는지 서버에 요청해야한다.
        confirmedId.value = id.value
        viewModelScope.launch {
            _idCheck.emit(true)
        }
    }

    fun samePasswordCheck() {
        viewModelScope.launch {
            _passwordCheck.emit(password.value == passwordAgain.value)
        }
    }

    fun requestSignUp(): Boolean = birthCheck.value && idCheck.value && passwordCheck.value
}
