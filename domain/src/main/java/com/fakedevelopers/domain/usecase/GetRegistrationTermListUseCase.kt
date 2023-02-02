package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.TermListDto
import com.fakedevelopers.domain.repository.RegistrationTermRepository
import javax.inject.Inject

class GetRegistrationTermListUseCase @Inject constructor(
    private val repository: RegistrationTermRepository
) {
    suspend operator fun invoke(): Result<TermListDto> =
        repository.getRegistrationTermList()
}
