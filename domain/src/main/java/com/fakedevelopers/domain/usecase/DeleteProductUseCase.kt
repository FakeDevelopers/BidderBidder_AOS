package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ProductEditorRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductEditorRepository
) {
    suspend operator fun invoke(productId: Long) = repository.postDeleteProduct(productId)
}
