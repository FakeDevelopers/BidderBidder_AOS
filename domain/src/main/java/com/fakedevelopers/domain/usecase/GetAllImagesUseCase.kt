package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetAllImagesUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    suspend operator fun invoke() = repository.getAllImages()
}
