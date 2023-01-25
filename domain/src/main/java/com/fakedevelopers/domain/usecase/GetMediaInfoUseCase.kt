package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.MediaInfo
import com.fakedevelopers.domain.repository.ImageRepository
import javax.inject.Inject

class GetMediaInfoUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(uri: String): MediaInfo = repository.getMediaInfo(uri)
}
