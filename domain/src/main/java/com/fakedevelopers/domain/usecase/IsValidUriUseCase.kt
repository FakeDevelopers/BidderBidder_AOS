package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class IsValidUriUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(uri: String): Boolean = repository.isValid(uri)
}
