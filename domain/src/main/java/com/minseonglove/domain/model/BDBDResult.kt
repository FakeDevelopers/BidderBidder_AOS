package com.minseonglove.domain.model

sealed class BDBDResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : BDBDResult<T>()
    data class Error(val e: Exception) : BDBDResult<Nothing>()
}
