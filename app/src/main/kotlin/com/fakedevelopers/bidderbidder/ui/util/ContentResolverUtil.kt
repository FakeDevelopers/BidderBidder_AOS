package com.fakedevelopers.bidderbidder.ui.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.fakedevelopers.bidderbidder.ui.productRegistration.albumList.UpdatedAlbumItem

class ContentResolverUtil(
    private val contentResolver: ContentResolver
) {

    fun isExist(uri: Uri): Boolean {
        contentResolver.runCatching {
            openFileDescriptor(uri, "r")
        }.onSuccess {
            it?.let {
                it.close()
                return true
            }
        }
        return false
    }

    fun getValidList(uriList: List<String>): List<String> {
        // 유효한 선택 이미지 리스트
        val validSelectedImageList = uriList.filter { isExist(Uri.parse(it)) }
        // 유효하지 않은 선택 이미지 리스트
        val invalidSelectedImageList = uriList.filter {
            !validSelectedImageList.contains(it)
        }
        return if (invalidSelectedImageList.isNotEmpty()) validSelectedImageList else uriList
    }

    fun getDateModifiedFromUri(uri: Uri): UpdatedAlbumItem? =
        contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
            ),
            null,
            null,
            null
        )?.let { cursor ->
            var updatedAlbumItem: UpdatedAlbumItem? = null
            val result = cursor.moveToNext()
            if (result) {
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                val token = path.split("/")
                val modified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED))
                updatedAlbumItem = UpdatedAlbumItem(uri.toString(), token[token.lastIndex - 1], modified)
            }
            cursor.close()
            updatedAlbumItem
        }
}
