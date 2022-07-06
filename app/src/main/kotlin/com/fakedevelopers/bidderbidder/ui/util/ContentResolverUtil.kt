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
}
