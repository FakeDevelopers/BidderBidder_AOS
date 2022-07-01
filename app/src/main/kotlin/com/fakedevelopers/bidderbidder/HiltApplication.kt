package com.fakedevelopers.bidderbidder

import android.app.Application
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HiltApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())
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
