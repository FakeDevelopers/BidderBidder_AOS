package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.TermListDto

interface RegistrationTermRepository {
    suspend fun getRegistrationTermList(): Result<TermListDto>

    suspend fun getRegistrationTermContents(id: Long): Result<String>
}
