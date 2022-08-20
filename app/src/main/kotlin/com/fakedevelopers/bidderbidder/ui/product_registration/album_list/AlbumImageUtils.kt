package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.webkit.MimeTypeMap
import java.util.Locale

class AlbumImageUtils(val context: Context) {
    fun getBitmapByURI(uri: String): Bitmap? {
        runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, Uri.parse(uri)))
        }.onSuccess {
            return it
        }
        return null
    }

    fun getMimeTypeAndExtension(uri: String): Pair<String, String> {
        val mimeType = context.contentResolver.getType(Uri.parse(uri)).toString()
        var extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString()
        if (extension == "jpg") {
            extension = "jpeg"
        }
        return mimeType to extension.uppercase(Locale.getDefault())
    }

    fun getRotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degree) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) ?: bitmap
    }

    companion object {
        // 1회 회전 각도
        const val ROTATE_DEGREE = 90f
    }
}
