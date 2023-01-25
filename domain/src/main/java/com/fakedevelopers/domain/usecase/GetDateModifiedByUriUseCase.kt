package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetDateModifiedByUriUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(uri: String): AlbumItem? = repository.getDateModifiedByUri(uri)
}
