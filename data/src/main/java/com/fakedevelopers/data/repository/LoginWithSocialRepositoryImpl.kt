package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.LoginWithSocialService
import com.fakedevelopers.domain.model.LoginInfo
import com.fakedevelopers.domain.repository.LoginWithSocialRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoginWithSocialRepositoryImpl @Inject constructor(
    private val service: LoginWithSocialService,
    private val auth: FirebaseAuth
) : LoginWithSocialRepository {
    override suspend fun loginWithGoogle(idToken: String): Result<LoginInfo> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = signInWithCredential(credential).first()
        return if (result.isSuccessful) {
            runCatching {
                service.loginWithGoogle()
            }
        } else {
            Result.failure(result.exception ?: Exception())
        }
    }

    private fun signInWithCredential(
        credential: AuthCredential
    ): Flow<Task<AuthResult>> = callbackFlow {
        auth.signInWithCredential(credential).addOnCompleteListener {
            trySend(it)
        }
        awaitClose()
    }
}
