package com.fakedevelopers.presentation.ui.util

object PriceUtil {
    fun priceToLong(price: String) = price.replace("[^\\d]".toRegex(), "").toLongOrNull()
    fun priceToInt(price: String) = price.replace("[^\\d]".toRegex(), "").toIntOrNull()
}
