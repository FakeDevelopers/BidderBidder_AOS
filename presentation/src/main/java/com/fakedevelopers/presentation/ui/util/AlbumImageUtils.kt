package com.fakedevelopers.presentation.ui.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AlbumImageUtils(
    private val contentResolver: ContentResolver
) {
    // getBitmap은 API 29에서 Deprecated 됐읍니다.
    @Suppress("DEPRECATION")
    suspend fun getBitmapByURI(uri: String, dispatcher: CoroutineDispatcher = Dispatchers.IO) =
        withContext(dispatcher) {
            var bitmap: Bitmap? = null
            runCatching {
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, Uri.parse(uri)))
                } else {
                    // API 28 이하는 createSource 사용 불가
                    MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(uri))
                }
            }.onSuccess {
                bitmap = it
            }
            bitmap
        }

    fun getMimeTypeAndExtension(uri: String): Pair<String, String> {
        val mimeType = contentResolver.getType(Uri.parse(uri)).toString()
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
