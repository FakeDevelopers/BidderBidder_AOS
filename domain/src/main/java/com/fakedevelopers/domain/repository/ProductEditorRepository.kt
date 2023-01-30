package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.ProductEditorInfo

interface ProductEditorRepository {
    suspend fun postProductModification(
        productId: Long,
        productEditorInfo: ProductEditorInfo
    ): Result<String>

    suspend fun postProductRegistration(
        productEditorInfo: ProductEditorInfo
    ): Result<String>
}
