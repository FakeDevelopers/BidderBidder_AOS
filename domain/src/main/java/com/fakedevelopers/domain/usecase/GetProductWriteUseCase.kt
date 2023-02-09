package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.ProductWriteDto
import com.fakedevelopers.domain.repository.LocalStorageRepository
import javax.inject.Inject

class GetProductWriteUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    suspend operator fun invoke(): ProductWriteDto =
        repository.getProductWrite()
}
