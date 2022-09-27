package com.fakedevelopers.bidderbidder.ui.util

import com.google.gson.Gson
import com.orhanobut.logger.Logger
import okhttp3.ResponseBody

object ApiErrorHandler {
    fun print(errorBody: ResponseBody?) {
        if (errorBody == null) {
            return
        }
        runCatching {
            val map = Gson().fromJson(errorBody.string(), Map::class.java)
            val sb = StringBuilder()
            map.keys.map { sb.appendLine("$it = ${map[it]}") }
            Logger.e(sb.toString())
        }
    }
}
