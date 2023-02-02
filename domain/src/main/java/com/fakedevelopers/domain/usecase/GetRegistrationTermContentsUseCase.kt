package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.RegistrationTermRepository
import javax.inject.Inject

class GetRegistrationTermContentsUseCase @Inject constructor(
    private val repository: RegistrationTermRepository
) {
    suspend operator fun invoke(id: Long): Result<String> =
        repository.getRegistrationTermContents(id)
}
