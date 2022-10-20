package com.fakedevelopers.bidderbidder.ui.util

import java.text.DecimalFormat

object PriceUtil {
    private val dec = DecimalFormat("#,###")

    fun numberToPrice(num: Number, postFix: String = "원") = "${dec.format(num)}$postFix"

    fun priceToLong(price: String) = price.replace("[^\\d]".toRegex(), "").toLongOrNull()

    fun priceToInt(price: String) = price.replace("[^\\d]".toRegex(), "").toLongOrNull()
}
