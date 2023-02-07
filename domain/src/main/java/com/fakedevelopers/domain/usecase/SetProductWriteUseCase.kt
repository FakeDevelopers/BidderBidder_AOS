package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.ProductWriteDto
import com.fakedevelopers.domain.repository.LocalStorageRepository
import javax.inject.Inject

class SetProductWriteUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    suspend operator fun invoke(productWriteDto: ProductWriteDto? = null): Boolean =
        productWriteDto?.let {
            repository.setProductWrite(it)
        } ?: repository.clearProductWrite()
}
