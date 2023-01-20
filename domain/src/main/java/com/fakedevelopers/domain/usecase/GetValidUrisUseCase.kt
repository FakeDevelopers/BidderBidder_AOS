package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetValidUrisUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(uris: List<String>): List<String> = repository.getValidUris(uris)
}
