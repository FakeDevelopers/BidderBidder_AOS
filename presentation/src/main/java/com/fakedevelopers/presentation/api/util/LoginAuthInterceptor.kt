package com.fakedevelopers.presentation.api.util

import com.fakedevelopers.domain.model.BEARER_TOKEN_PREFIX
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class LoginAuthInterceptor @Inject constructor(
    private val firebase: FirebaseAuth
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
        val newRequest = request().newBuilder()
        firebase.currentUser?.getIdToken(false)?.let {
            val authorization = BEARER_TOKEN_PREFIX + it.result.token
            newRequest.addHeader("Authorization", authorization)
        }
        proceed(newRequest.build())
    }
}
