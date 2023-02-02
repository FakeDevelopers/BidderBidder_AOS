package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.RegistrationTermService
import com.fakedevelopers.domain.model.TermListDto
import com.fakedevelopers.domain.repository.RegistrationTermRepository
import javax.inject.Inject

class RegistrationTermRepositoryImpl @Inject constructor(
    private val service: RegistrationTermService
) : RegistrationTermRepository {
    override suspend fun getRegistrationTermList(): Result<TermListDto> =
        runCatching {
            service.getRegistrationTermList()
        }

    override suspend fun getRegistrationTermContents(id: Long): Result<String> =
        runCatching {
            service.getRegistrationTermContents(id)
        }
}
