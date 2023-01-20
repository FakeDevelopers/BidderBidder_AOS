package com.fakedevelopers.presentation.ui.util

fun String.priceToLong() = replace("[^\\d]".toRegex(), "").toLongOrNull()
fun String.priceToInt() = replace("[^\\d]".toRegex(), "").toIntOrNull()
