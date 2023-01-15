package com.fakedevelopers.bidderbidder

import android.app.Application
import com.fakedevelopers.domain.secret.Constants.Companion.SENTRY_DSN
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid

@HiltAndroidApp
class HiltApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
        initSentry()
    }

    private fun initSentry() {
        SentryAndroid.init(this) { options ->
            options.dsn = SENTRY_DSN
        }
    }
}
