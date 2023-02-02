package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.model.ProductCategoryDto
import com.fakedevelopers.domain.repository.ProductCategoryRepository
import javax.inject.Inject

class GetProductCategoryUseCase @Inject constructor(
    private val repository: ProductCategoryRepository
) {
    suspend operator fun invoke(): Result<List<ProductCategoryDto>> =
        repository.getProductCategory()
}
