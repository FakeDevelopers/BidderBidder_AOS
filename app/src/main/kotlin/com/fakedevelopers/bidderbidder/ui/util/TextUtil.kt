package com.fakedevelopers.bidderbidder.ui.util

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan

class TextUtil(
    private val context: Context
) {
    fun setPartialColor(text: CharSequence, colorId: Int, start: Int, end: Int) =
        SpannableStringBuilder(text).apply {
            setSpan(
                ForegroundColorSpan(context.getColor(colorId)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    fun setPartialRelativeSize(text: CharSequence, increaseTime: Float, start: Int, end: Int) =
        SpannableStringBuilder(text).apply {
            setSpan(
                RelativeSizeSpan(increaseTime),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

    fun setPartialStyle(text: CharSequence, style: Int, start: Int, end: Int) =
        SpannableStringBuilder(text).apply {
            setSpan(
                StyleSpan(style),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
}
