package com.fakedevelopers.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
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

    override suspend fun getImages(path: String?): List<AlbumItem> {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val where = path?.let { MediaStore.Images.Media.DATA + it }
        val images = mutableListOf<AlbumItem>()
        contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
            ),
            where,
            null,
            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                // 이미지 Uri
                val imageUri = ContentUris.withAppendedId(
                    uri,
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                ).toString()
                // 최근 수정 날짜
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                images.add(AlbumItem(imageUri, date))
            }
        }
        return images
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
        var extension = mimeType.substringAfter('/')
        if (extension == "jpg") {
            extension = "jpeg"
        }
        return MediaInfo(mimeType, extension.uppercase(Locale.getDefault()))
    }

    override fun getRotate(uri: String): Float {
        runCatching {
            ExifInterface(uri.getPath()!!).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.onSuccess { attr ->
            return when (attr) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }
        return 0f
    }

    private fun String.getPath() =
        contentResolver.query(
            Uri.parse(this),
            arrayOf(MediaStore.Images.Media.DATA),
            null,
            null,
            null
        )?.use { cursor ->
            cursor.moveToNext()
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
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
