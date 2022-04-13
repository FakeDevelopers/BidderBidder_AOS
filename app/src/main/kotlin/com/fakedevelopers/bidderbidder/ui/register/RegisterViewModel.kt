package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    val firebaseToken = MutableLiveData("")
    val birth = MutableLiveData("")
    val birthCheck = MutableLiveData(false)
    val id = MutableLiveData("")
    private val confirmedId = MutableLiveData("")
    val idCheck = MutableLiveData(false)
    val password = MutableLiveData("")
    val passwordAgain = MutableLiveData("")
    val passwordCheck = MutableLiveData(false)

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
