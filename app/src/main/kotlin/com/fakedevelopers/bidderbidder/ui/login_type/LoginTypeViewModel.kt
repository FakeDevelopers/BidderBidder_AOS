package com.fakedevelopers.bidderbidder.ui.login_type

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.SigninGoogleRepository
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
    private var token: String = ""

    private val _signinGoogleResponse = MutableSharedFlow<Response<SigninGoogleDto>>()
    private val bearer = "Bearer "

    val auth get() = _auth
    val signinGoogleResponse: SharedFlow<Response<SigninGoogleDto>> get() = _signinGoogleResponse

    private fun signinGoogleRequest() {
        viewModelScope.launch {
            _signinGoogleResponse.emit(repository.postSigninGoogle(token))
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        _auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.user!!.getIdToken(true).addOnSuccessListener { result ->
                    token = bearer + result.token
                    signinGoogleRequest()
                }
            } else {
                Logger.e(task.exception.toString())
            }
        }
    }
}
