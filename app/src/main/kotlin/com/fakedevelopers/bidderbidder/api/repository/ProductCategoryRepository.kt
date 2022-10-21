package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ProductCategoryService
import com.fakedevelopers.bidderbidder.ui.productRegistration.ProductCategoryDto
import retrofit2.Response
import javax.inject.Inject

class ProductCategoryRepository @Inject constructor(
    private val service: ProductCategoryService
) {
    suspend fun getProductCategory(): Response<List<ProductCategoryDto>> {
        return service.getProductRegistrationCategory()
    }
}
