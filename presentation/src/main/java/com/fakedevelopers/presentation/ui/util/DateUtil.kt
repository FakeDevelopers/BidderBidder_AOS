package com.fakedevelopers.presentation.ui.util

import android.content.Context
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.model.RemainTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DateUtil(
    private val context: Context
) {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    fun getRemainTimeMillisecond(date: String) =
        formatter.parse(date)?.run { time - System.currentTimeMillis() }

    fun getRemainTimeString(remainTime: RemainTime): String {
        val list = mutableListOf<String>()
        if (remainTime.day > 0) {
            list.add(context.getString(R.string.time_days, remainTime.day))
        }
        if (remainTime.day > 0 || remainTime.hour > 0) {
            list.add(context.getString(R.string.time_hours, remainTime.hour))
        }
        if (remainTime.day > 0 || remainTime.hour > 0 || remainTime.minute > 0) {
            list.add(context.getString(R.string.time_minutes, remainTime.minute))
        }
        if (remainTime.day == 0L) {
            list.add(context.getString(R.string.time_seconds, remainTime.second))
        }
        return list.joinToString(" ")
    }
}
