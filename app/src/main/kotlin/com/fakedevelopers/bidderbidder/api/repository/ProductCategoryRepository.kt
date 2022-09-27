package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.service.ProductCategoryService
import com.fakedevelopers.bidderbidder.ui.product_registration.ProductCategoryDto
import retrofit2.Response
import javax.inject.Inject

class ProductCategoryRepository @Inject constructor(
    private val service: ProductCategoryService
) {
    suspend fun getProdectCategory(): Response<List<ProductCategoryDto>> {
        return service.getProductRegistrationCategory()
    }
}
