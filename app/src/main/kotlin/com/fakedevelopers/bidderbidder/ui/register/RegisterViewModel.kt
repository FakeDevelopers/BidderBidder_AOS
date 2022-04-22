package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableStateFlow("")
    val id = MutableStateFlow("")
    val password = MutableStateFlow("")
    val passwordAgain = MutableStateFlow("")

    private val confirmedId = MutableStateFlow("")
    private val _birth = MutableStateFlow("")
    private val _eventFlow = MutableSharedFlow<RegisterEvent>()

    val birth: StateFlow<String> get() = _birth
    val eventFlow: SharedFlow<RegisterEvent> get() = _eventFlow

    fun setBirth(userBirth: String) {
        // 연령에 따라 경고 메세지 추가 예정
        _birth.value = userBirth
        event(RegisterEvent.BirthCheck(true))
    }

    fun idDuplicationCheck() {
        // 여기서 id.value와 중복되는 id가 있는지 서버에 요청해야한다.
        confirmedId.value = id.value
        event(RegisterEvent.IdCheck(true))
    }

    fun samePasswordCheck() {
        event(RegisterEvent.PasswordCheck(password.value == passwordAgain.value))
    }

    // 비밀번호는 samePasswordCheck에서 걸러준다.
    // 현재는 아이디에 변경이 없었는지만 검사한다.
    fun requestSignUp() = confirmedId.value == id.value

    private fun event(event: RegisterEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    sealed class RegisterEvent {
        data class BirthCheck(val check: Boolean) : RegisterEvent()
        data class IdCheck(val check: Boolean) : RegisterEvent()
        data class PasswordCheck(val check: Boolean) : RegisterEvent()
    }
}
