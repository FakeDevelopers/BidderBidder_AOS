package com.fakedevelopers.presentation.ui.util

import com.google.gson.Gson
import com.orhanobut.logger.Logger
import io.sentry.Sentry
import okhttp3.ResponseBody

object ApiErrorHandler {
    fun printErrorMessage(errorBody: ResponseBody?) {
        if (errorBody == null) {
            return
        }
        runCatching {
            val map = Gson().fromJson(errorBody.string(), Map::class.java)
            val sb = StringBuilder()
            map.keys.map { sb.appendLine("$it = ${map[it]}") }
            Sentry.captureMessage(sb.toString())
            Logger.e(sb.toString())
        }
    }

    fun printMessage(message: String?) {
        if (!message.isNullOrEmpty()) {
            Sentry.captureMessage(message)
            Logger.e(message)
        }
    }
}
