package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.ByteArrayOutputStream

class AlbumImageUtils(val context: Context) {
    // 회전
    private val matrix = Matrix().apply {
        postRotate(ROTATE_DEGREE)
    }

    fun getBitmapByURI(uri: String): Bitmap? {
        runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, Uri.parse(uri)))
        }.onSuccess {
            return it
        }
        return null
    }

    fun getURIByBitmap(bitmap: Bitmap) {
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, ByteArrayOutputStream())
    }

    fun getMimeTypeAndExtension(uri: String): Pair<String, String> {
        val mimeType = context.contentResolver.getType(Uri.parse(uri)).toString()
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString()
        return mimeType to extension
    }

    fun getRotateBitmap(bitmap: Bitmap) =
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true) ?: bitmap

    companion object {
        // 회전 각도
        const val ROTATE_DEGREE = 90f
    }
}
