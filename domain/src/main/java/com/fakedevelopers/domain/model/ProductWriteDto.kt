package com.fakedevelopers.domain.model

data class ProductWriteDto(
    val title: String,
    val hopePrice: String,
    val openingBid: String,
    val tick: String,
    val expiration: String,
    val content: String,
    val categoryId: Long
)
