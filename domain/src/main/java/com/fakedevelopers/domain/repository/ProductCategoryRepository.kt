package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.ProductCategoryDto

interface ProductCategoryRepository {
    suspend fun getProductCategory(): Result<List<ProductCategoryDto>>
}
