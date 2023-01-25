package com.fakedevelopers.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.domain.model.MediaInfo
import com.fakedevelopers.domain.repository.ImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver
) : ImageRepository {
    override fun isValid(uri: String): Boolean {
        contentResolver.runCatching {
            openFileDescriptor(Uri.parse(uri), "r")?.use {
                return true
            }
        }
        return false
    }

    override fun getValidUris(uris: List<String>): List<String> =
        uris.filter { uri -> isValid(uri) }

    override suspend fun getBytesByUri(uri: String, dispatcher: CoroutineDispatcher): ByteArray? =
        withContext(dispatcher) {
            var result: ByteArray? = null
            runCatching {
                contentResolver.openInputStream(Uri.parse(uri))
            }.onSuccess { inputStream ->
                inputStream?.use {
                    result = it.readBytes()
                }
            }
            result
        }

    override fun getMediaInfo(uri: String): MediaInfo {
        val mimeType = contentResolver.getType(Uri.parse(uri)).toString()
        var extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString()
        if (extension == "jpg") {
            extension = "jpeg"
        }
        return MediaInfo(mimeType, extension.uppercase(Locale.getDefault()))
    }

    override fun getDateModifiedByUri(uri: String): AlbumItem? =
        contentResolver.query(
            Uri.parse(uri),
            arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
            ),
            null,
            null,
            null
        )?.use { cursor ->
            var updatedAlbumItem: AlbumItem? = null
            if (cursor.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                val relPath = path.substringBeforeLast("/")
                updatedAlbumItem = AlbumItem(
                    uri = uri,
                    modified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED)),
                    path = relPath
                )
            }
            updatedAlbumItem
        }
}
