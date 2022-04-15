package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableLiveData("")
    val birth = MutableLiveData("")
    val id = MutableLiveData("")
    val password = MutableLiveData("")
    val passwordAgain = MutableLiveData("")

    private val confirmedId = MutableLiveData("")
    private val _birthCheck = MutableLiveData(false)
    private val _idCheck = MutableLiveData(false)
    private val _passwordCheck = MutableLiveData(false)

    val birthCheck: LiveData<Boolean> get() = _birthCheck
    val idCheck: LiveData<Boolean> get() = _idCheck
    val passwordCheck: LiveData<Boolean> get() = _passwordCheck

    fun idDuplicationCheck() {
        // 여기서 id.value와 중복되는 id가 있는지 서버에 요청해야한다.
        confirmedId.value = id.value
        if(!idCheck.value!!){
            _idCheck.value = true
        }
    }

    fun samePasswordCheck() {
        _passwordCheck.value = password.value == passwordAgain.value
    }

    fun requestSignUp(): Boolean = birthCheck.value!! && idCheck.value!! && _passwordCheck.value!!
}
