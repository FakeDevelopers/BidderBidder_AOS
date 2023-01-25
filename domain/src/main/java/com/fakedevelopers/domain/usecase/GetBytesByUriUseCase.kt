package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetBytesByUriUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    suspend operator fun invoke(uri: String) = repository.getBytesByUri(uri)
}
