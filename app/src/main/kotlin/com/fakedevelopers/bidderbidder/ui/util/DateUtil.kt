package com.fakedevelopers.bidderbidder.ui.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DateUtil {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    fun getRemainTimeMillisecond(expirationDate: String) =
        formatter.parse(expirationDate)?.run { time - System.currentTimeMillis() }
}
