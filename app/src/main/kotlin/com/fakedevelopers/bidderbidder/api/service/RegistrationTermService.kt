package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.register.acceptTerms.TermListDto
import retrofit2.Response
import retrofit2.http.GET

interface RegistrationTermService {
    @GET("term/list")
    suspend fun getRegistrationTermList(): Response<TermListDto>
}
