package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.AlbumItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface ImageRepository {
    fun isValid(uri: String): Boolean
    fun getValidUris(uris: List<String>): List<String>
    fun getDateModifiedByUri(uri: String): AlbumItem?
    suspend fun getBytesByUri(uri: String, dispatcher: CoroutineDispatcher = Dispatchers.IO): ByteArray?
}
