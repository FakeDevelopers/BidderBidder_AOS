package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumInfoUseCase @Inject constructor(
    private val getImagesUseCase: GetImagesUseCase,
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(allImagesTitle: String) =
        albumRepository.getAlbumInfo(
            albumItems = getImagesUseCase(),
            allImagesTitle = allImagesTitle
        )
}
