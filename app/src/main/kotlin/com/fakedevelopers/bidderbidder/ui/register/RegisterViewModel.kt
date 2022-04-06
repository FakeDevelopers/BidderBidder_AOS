package com.fakedevelopers.bidderbidder.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class RegisterViewModel: ViewModel() {
    val phoneNumber = MutableLiveData<String>()
    val authCode = MutableLiveData<String>()
    val isCodeSending = MutableLiveData<Boolean>()
    val verificationId = MutableLiveData<String>()
    val currentUser = MutableLiveData<FirebaseUser>()

    init {
        isCodeSending.value = false
    }
}
