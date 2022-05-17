package com.fakedevelopers.bidderbidder.api.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

object ImageLoader {
    private val imageCache = mutableMapOf<String, Bitmap>()

    fun loadProductImage(url: String, completed: (Bitmap?) -> Unit) {
        if (url.isEmpty()) {
            completed(null)
            return
        }

        // 한 번 받아온 이미지는 캐싱한거 갖다 쓴다
        if (imageCache.containsKey(url)) {
            completed(imageCache[url])
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                BitmapFactory.decodeStream(URL(url).openStream()).let {
                    imageCache[url] = it
                    withContext(Dispatchers.Main) {
                        completed(it)
                    }
                }
            } catch (e: Exception) {
                Logger.e(e.message!!)
                withContext(Dispatchers.Main) {
                    completed(null)
                }
            }
        }
    }
}
