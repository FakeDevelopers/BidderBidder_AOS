package com.fakedevelopers.data.repository

import com.fakedevelopers.data.service.ProductCategoryService
import com.fakedevelopers.domain.model.ProductCategoryDto
import com.fakedevelopers.domain.repository.ProductCategoryRepository
import javax.inject.Inject

class ProductCategoryRepositoryImpl @Inject constructor(
    private val service: ProductCategoryService
) : ProductCategoryRepository {
    override suspend fun getProductCategory(): Result<List<ProductCategoryDto>> =
        runCatching {
            service.getProductRegistrationCategory()
        }
}
