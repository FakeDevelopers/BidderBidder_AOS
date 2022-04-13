package com.fakedevelopers.bidderbidder.ui.register

import android.os.Build
import android.text.Editable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableLiveData("")
    val birth = MutableLiveData("")
    val birthCheck = MutableLiveData(false)
    val birthFocusable = MutableLiveData(false)
    val id = MutableLiveData("")
    private val confirmedId = MutableLiveData("")
    val idCheck = MutableLiveData(false)
    val password = MutableLiveData("")
    val passwordAgain = MutableLiveData("")
    val passwordCheck = MutableLiveData(false)

    fun birthFormCheck(editable: Editable?) {
        // 하위 버전
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            birthCheck.value = editable.toString().length == 8
        }
    }

    fun idDuplicationCheck() {
        // 여기서 id.value와 중복되는 id가 있는지 서버에 요청해야한다.
        confirmedId.value = id.value
        if(!idCheck.value!!){
            idCheck.value = true
        }
    }

    fun samePasswordCheck() {
        passwordCheck.value = password.value == passwordAgain.value
    }

    fun requestSignUp(): Boolean = birthCheck.value!! && idCheck.value!! && passwordCheck.value!!
}
