package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhoneAuthViewModel : ViewModel() {

    val phoneNumber = MutableLiveData<String>()
    val authCode = MutableLiveData<String>()
    val isCodeSending = MutableLiveData(false)
    val verificationId = MutableLiveData<String>()

}
