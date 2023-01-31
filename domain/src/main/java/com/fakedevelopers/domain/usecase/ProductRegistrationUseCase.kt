package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.ProductEditorInfo
import com.fakedevelopers.domain.repository.ProductEditorRepository
import javax.inject.Inject

class ProductRegistrationUseCase @Inject constructor(
    private val repository: ProductEditorRepository
) {
    suspend operator fun invoke(
        productEditorInfo: ProductEditorInfo
    ): Result<String> =
        repository.postProductRegistration(
            productEditorInfo
        )
}
