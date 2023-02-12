package com.fakedevelopers.data.repository

import com.fakedevelopers.domain.model.AlbumInfo
import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.domain.repository.AlbumRepository

class AlbumRepositoryImpl : AlbumRepository {
    override fun getAlbumInfo(
        albumItems: List<AlbumItem>,
        allImagesTitle: String
    ): List<AlbumInfo> {
        val albumInfo = mutableListOf<AlbumInfo>()
        albumInfo.add(
            AlbumInfo(
                path = "",
                firstImage = albumItems[0].uri,
                name = allImagesTitle,
                count = albumItems.size
            )
        )
        val countMap = mutableMapOf<String, Int>()
        albumItems.forEach { albumItem ->
            val relPath = albumItem.path.substringBeforeLast('/')
            countMap[relPath] = (countMap[relPath] ?: 0) + 1
        }
        countMap.keys.forEach { relPath ->
            albumInfo.add(
                AlbumInfo(
                    path = relPath,
                    firstImage = albumItems.find { it.path.contains(relPath) }?.uri ?: "",
                    name = relPath.substringAfterLast('/'),
                    count = countMap[relPath] ?: 0
                )
            )
        }
        return albumInfo
    }
}
