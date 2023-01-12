package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.RegistrationTermService
import com.fakedevelopers.presentation.ui.register.acceptTerms.TermListDto
import retrofit2.Response
import javax.inject.Inject

class RegistrationTermRepository @Inject constructor(
    private val service: RegistrationTermService
) {
    suspend fun getRegistrationTermList(): Response<TermListDto> {
        return service.getRegistrationTermList()
    }

    suspend fun getRegistrationTermContents(id: Long): Response<String> {
        return service.getRegistrationTermContents(id)
    }
}
