package com.fakedevelopers.bidderbidder.ui.util

import android.content.Context
import android.net.Uri

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
}
