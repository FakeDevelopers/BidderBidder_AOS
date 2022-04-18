package com.fakedevelopers.bidderbidder.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableLiveData("")
    val id = MutableLiveData("")
    val password = MutableLiveData("")
    val passwordAgain = MutableLiveData("")

    private lateinit var confirmedId: String
    private val _birth = MutableLiveData("")
    private val _birthCheck = MutableLiveData(false)
    private val _idCheck = MutableLiveData(false)
    private val _passwordCheck = MutableLiveData(false)

    val birth: LiveData<String> get() = _birth
    val birthCheck: LiveData<Boolean> get() = _birthCheck
    val idCheck: LiveData<Boolean> get() = _idCheck
    val passwordCheck: LiveData<Boolean> get() = _passwordCheck

    fun setBirth(year: Int, month: Int, dayOfMonth: Int) {
        _birth.value = "${year}년 ${month + 1}월 ${dayOfMonth}일"
        if (!birthCheck.value!!) {
            Log.d("mytest", "asdf")
            _birthCheck.value = true
        }
    }

    fun idDuplicationCheck() {
        // 여기서 id.value와 중복되는 id가 있는지 서버에 요청해야한다.
        confirmedId = id.value!!
        if (!idCheck.value!!) {
            _idCheck.value = true
        }
    }

    fun samePasswordCheck() {
        _passwordCheck.value = password.value == passwordAgain.value
    }

    fun requestSignUp(): Boolean = birthCheck.value!! && idCheck.value!! && passwordCheck.value!!
}
