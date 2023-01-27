package com.fakedevelopers.presentation.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.fakedevelopers.domain.model.MediaInfo
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

private const val COMPRESS_QUALITY = 70
const val ROTATE_DEGREE = 90f

fun ByteArray.toBitmap(): Bitmap = BitmapFactory.decodeByteArray(this, 0, size)
fun Bitmap.getRotatedBitmap(degree: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degree) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true) ?: this
}
fun Bitmap.getMultipart(mediaInfo: MediaInfo): MultipartBody.Part {
    val requestBody = object : RequestBody() {
        override fun contentType(): MediaType {
            return mediaInfo.mimeType.toMediaType()
        }
        override fun writeTo(sink: BufferedSink) {
            compress(
                Bitmap.CompressFormat.valueOf(mediaInfo.extension),
                COMPRESS_QUALITY,
                sink.outputStream()
            )
        }
    }
    return MultipartBody.Part.createFormData("files", createFileName(mediaInfo.extension), requestBody)
}

private fun createFileName(extension: String) =
    "${System.currentTimeMillis()}.${extension.lowercase()}"
