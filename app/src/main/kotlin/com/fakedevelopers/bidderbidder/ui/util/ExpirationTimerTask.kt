package com.fakedevelopers.bidderbidder.ui.util

import android.os.CountDownTimer

class ExpirationTimerTask(
    remainTime: Long,
    countDownInterval: Long = 1000L,
    private val tick: ((String) -> Unit)? = null,
    private val finish: (() -> Unit)? = null
) : CountDownTimer(remainTime, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        tick?.invoke(getRemainTimeString(millisUntilFinished))
    }

    override fun onFinish() {
        finish?.invoke()
    }

    private fun getRemainTimeString(millisUntilFinished: Long): String {
        val totalMinute = millisUntilFinished / 60000
        val day = totalMinute / 1440
        val hour = totalMinute % 1440 / 60
        val remainTimeString = StringBuilder()
        // 일
        if (day > 0) {
            remainTimeString.append("${day}일 ")
        }
        // 시간
        if (hour != 0L) {
            remainTimeString.append("${hour}시간 ")
        }
        // 분, 초
        if (day == 0L && hour < 3) {
            val minute = totalMinute % 60
            if (minute != 0L) {
                remainTimeString.append("${minute}분 ")
            }
            if (hour == 0L && minute < 5) {
                remainTimeString.append("${millisUntilFinished % 60000 / 1000}초")
            }
        }
        return remainTimeString.toString()
    }
}
