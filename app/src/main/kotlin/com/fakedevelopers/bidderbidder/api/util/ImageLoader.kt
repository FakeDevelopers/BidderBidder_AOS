package com.fakedevelopers.bidderbidder.api.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.URL

object ImageLoader {
    private val imageCache = mutableMapOf<String, Bitmap>()

    suspend fun loadProductImage(
        url: String,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
        completed: (Bitmap?) -> Unit
    ) {
        if (url.isEmpty()) {
            completed(null)
            return
        }

        // 한 번 받아온 이미지는 캐싱한거 갖다 쓴다
        if (imageCache.containsKey(url)) {
            completed(imageCache[url])
            return
        }

        runCatching {
            BitmapFactory.decodeStream(URL(url).openStream()).let {
                imageCache[url] = it
                coroutineScope {
                    launch(dispatcher) {
                        completed(it)
                    }
                }
            }
        }.onFailure {
            Logger.e(it.message.toString())
            coroutineScope {
                launch(dispatcher) {
                    completed(null)
                }
            }
        }
    }
}
