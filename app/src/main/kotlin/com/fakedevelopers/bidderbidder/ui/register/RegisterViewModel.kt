package com.fakedevelopers.bidderbidder.ui.register

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableLiveData("")
    val birth = MutableLiveData("")
    val isBirthCheck = MutableLiveData(false)
    val id = MutableLiveData("")
    private val confirmedId = MutableLiveData("")
    val isIdCheck = MutableLiveData(false)
    val password = MutableLiveData("")
    val passwordAgain = MutableLiveData("")
    val isPasswordCheck = MutableLiveData(false)

    fun idDuplicationCheck() {
        // 여기서 id.value와 중복되는 id가 있는지 서버에 요청해야한다.
        confirmedId.value = id.value
        if(!isIdCheck.value!!){
            isIdCheck.value = true
        }
        Log.d("register", confirmedId.value.toString())
    }

    fun samePasswordCheck() {
        isPasswordCheck.value = password.value == passwordAgain.value
    }

    fun requestSignUp(): Boolean {
        //여기서 정보가 올바른지 검사하고 토큰, birth, confirmedId, confirmedPassword를 서버로 보낸다.
        //지금은 값만 다적었는지 검사한다.
        return isBirthCheck.value!! && isIdCheck.value!! && isPasswordCheck.value!!
    }
}
