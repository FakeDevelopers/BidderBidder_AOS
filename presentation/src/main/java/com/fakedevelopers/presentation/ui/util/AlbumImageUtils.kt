package com.fakedevelopers.presentation.ui.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.webkit.MimeTypeMap
import java.util.Locale

class AlbumImageUtils(
    private val contentResolver: ContentResolver
) {
    fun getMimeTypeAndExtension(uri: String): Pair<String, String> {
        val mimeType = contentResolver.getType(Uri.parse(uri)).toString()
        var extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType).toString()
        if (extension == "jpg") {
            extension = "jpeg"
        }
        return mimeType to extension.uppercase(Locale.getDefault())
    }
}

const val ROTATE_DEGREE = 90f

fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, size)
fun Bitmap.getRotated(degree: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degree) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true) ?: this
}
