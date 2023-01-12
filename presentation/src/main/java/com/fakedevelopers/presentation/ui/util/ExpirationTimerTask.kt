package com.fakedevelopers.presentation.ui.util

import android.os.CountDownTimer
import com.fakedevelopers.presentation.model.RemainTime

class ExpirationTimerTask(
    remainTime: Long,
    countDownInterval: Long = 1000L,
    private val tick: ((RemainTime) -> Unit)? = null,
    private val finish: (() -> Unit)? = null
) : CountDownTimer(remainTime, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        tick?.invoke(getRemainTime(millisUntilFinished))
    }

    override fun onFinish() {
        finish?.invoke()
    }

    private fun getRemainTime(millisUntilFinished: Long): RemainTime {
        val totalMinute = millisUntilFinished / 60000
        return RemainTime(
            day = totalMinute / 1440,
            hour = totalMinute % 1440 / 60,
            minute = totalMinute % 60,
            second = millisUntilFinished % 60000 / 1000
        )
    }
}
