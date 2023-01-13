package com.fakedevelopers.presentation.api.service

import com.fakedevelopers.presentation.ui.productRegistration.ProductCategoryDto
import retrofit2.Response
import retrofit2.http.GET

interface ProductCategoryService {
    @GET("product/getAllCategory")
    suspend fun getProductRegistrationCategory(): Response<List<ProductCategoryDto>>
}
