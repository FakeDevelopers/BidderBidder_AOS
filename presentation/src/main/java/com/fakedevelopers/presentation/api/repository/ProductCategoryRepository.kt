package com.fakedevelopers.presentation.api.repository

import com.fakedevelopers.presentation.api.service.ProductCategoryService
import com.fakedevelopers.presentation.ui.productRegistration.ProductCategoryDto
import retrofit2.Response
import javax.inject.Inject

class ProductCategoryRepository @Inject constructor(
    private val service: ProductCategoryService
) {
    suspend fun getProductCategory(): Response<List<ProductCategoryDto>> {
        return service.getProductRegistrationCategory()
    }
}
