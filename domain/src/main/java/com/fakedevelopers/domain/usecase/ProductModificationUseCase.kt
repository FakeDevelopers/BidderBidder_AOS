package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.ProductEditorInfo
import com.fakedevelopers.domain.repository.ProductEditorRepository
import javax.inject.Inject

class ProductModificationUseCase @Inject constructor(
    private val repository: ProductEditorRepository
) {
    suspend operator fun invoke(
        productId: Long,
        productEditorInfo: ProductEditorInfo
    ): Result<String> =
        repository.postProductModification(
            productId,
            productEditorInfo
        )
}
