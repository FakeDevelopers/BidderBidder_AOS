package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetRotateUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(uri: String): Float = repository.getRotate(uri)
}
