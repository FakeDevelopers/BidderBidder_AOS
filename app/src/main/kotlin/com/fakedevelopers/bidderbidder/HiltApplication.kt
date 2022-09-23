package com.fakedevelopers.bidderbidder

import android.app.Application
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.SENTRY_DSN
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid

@HiltAndroidApp
class HiltApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        initSentry()
        try {
            throw Exception("야옹")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun initSentry() {
        SentryAndroid.init(this) { options ->
            options.dsn = SENTRY_DSN
        }
    }

    fun setPartialTextColor(text: CharSequence, colorId: Int, start: Int, end: Int) =
        SpannableStringBuilder(text).apply {
            setSpan(
                ForegroundColorSpan(applicationContext.getColor(colorId)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
}
