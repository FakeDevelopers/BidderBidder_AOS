package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetImagesUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    suspend operator fun invoke(path: String? = null): List<AlbumItem> =
        repository.getImages(path)
}
