package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.AlbumItem

interface ImageRepository {
    fun isValid(uri: String): Boolean
    fun getValidUris(uris: List<String>): List<String>
    fun getDateModifiedFromUri(uri: String): AlbumItem?
}
