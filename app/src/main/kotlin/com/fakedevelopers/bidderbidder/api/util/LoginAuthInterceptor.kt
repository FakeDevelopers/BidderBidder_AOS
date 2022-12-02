package com.fakedevelopers.bidderbidder.api.util

import com.fakedevelopers.bidderbidder.ui.util.HttpRequestExtensions
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
            val authorization = HttpRequestExtensions.BEARER_TOKEN_PREFIX + it.result.token
            newRequest.addHeader("Authorization", authorization)
        }
        proceed(newRequest.build())
    }
}
