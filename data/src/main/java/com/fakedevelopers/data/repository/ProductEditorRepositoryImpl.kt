package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.ProductEditorService
import com.fakedevelopers.domain.model.ProductEditorInfo
import com.fakedevelopers.domain.repository.ProductEditorRepository
import javax.inject.Inject

class ProductEditorRepositoryImpl @Inject constructor(
    private val service: ProductEditorService
) : ProductEditorRepository {
    override suspend fun postProductModification(
        productId: Long,
        productEditorInfo: ProductEditorInfo
    ): Result<String> {
        return runCatching {
            service.postProductModification(productId, productEditorInfo.getMap(), productEditorInfo.files)
        }
    }

    override suspend fun postProductRegistration(
        productEditorInfo: ProductEditorInfo
    ): Result<String> {
        return runCatching {
            service.postProductRegistration(productEditorInfo.getMap(), productEditorInfo.files)
        }
    }
}
