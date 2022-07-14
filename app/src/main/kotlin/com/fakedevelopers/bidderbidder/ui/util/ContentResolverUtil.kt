package com.fakedevelopers.bidderbidder.ui.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

class ContentResolverUtil(context: Context) {
    private val contentResolver = context.contentResolver

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

    fun getDateModifiedFromUri(uri: Uri): Pair<String, Long> {
        contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
            ),
            null,
            null,
            null
        )?.let {
            it.moveToNext()
            val relativePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
            val dateModified = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_MODIFIED))
            it.close()
            return relativePath to dateModified
        }
        return "" to 0L
    }
}
