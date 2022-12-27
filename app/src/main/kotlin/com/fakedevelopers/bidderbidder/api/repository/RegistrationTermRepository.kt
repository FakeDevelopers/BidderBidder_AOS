package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.RegistrationTermService
import com.fakedevelopers.bidderbidder.ui.register.acceptTerms.TermListDto
import retrofit2.Response
import javax.inject.Inject

class RegistrationTermRepository @Inject constructor(
    private val service: RegistrationTermService
) {
    suspend fun getRegistrationTermList(): Response<TermListDto> {
        return service.getRegistrationTermList()
    }
}
