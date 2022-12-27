package com.fakedevelopers.bidderbidder.ui.loginType

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.SigninGoogleRepository
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.orhanobut.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginTypeViewModel @Inject constructor(
    private val repository: SigninGoogleRepository,
    private val _auth: FirebaseAuth
) : ViewModel() {
    private val _signinGoogleResponse = MutableSharedFlow<Response<SigninGoogleDto>>()

    val signinGoogleResponse: SharedFlow<Response<SigninGoogleDto>> get() = _signinGoogleResponse

    private fun signinGoogleRequest() {
        viewModelScope.launch {
            repository.postSigninGoogle().let {
                if (it.isSuccessful) {
                    _signinGoogleResponse.emit(it)
                } else {
                    ApiErrorHandler.printErrorMessage(it.errorBody())
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        _auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                signinGoogleRequest()
            } else {
                Logger.e(task.exception.toString())
            }
        }
    }
}
