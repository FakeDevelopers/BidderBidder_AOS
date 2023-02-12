package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.AlbumInfo
import com.fakedevelopers.domain.model.AlbumItem

interface AlbumRepository {
    fun getAlbumInfo(albumItems: List<AlbumItem>, allImagesTitle: String): List<AlbumInfo>
}
