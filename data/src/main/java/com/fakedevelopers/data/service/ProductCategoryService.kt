package com.fakedevelopers.data.service

import com.fakedevelopers.domain.model.ProductCategoryDto
import retrofit2.http.GET

interface ProductCategoryService {
    @GET("product/getAllCategory")
    suspend fun getProductRegistrationCategory(): List<ProductCategoryDto>
}
