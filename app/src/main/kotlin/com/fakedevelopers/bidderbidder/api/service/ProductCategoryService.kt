package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.product_registration.ProductCategoryDto
import retrofit2.Response
import retrofit2.http.GET

interface ProductCategoryService {
    @GET("product/getAllCategory")
    suspend fun getProductRegistrationCategory(): Response<List<ProductCategoryDto>>
}
