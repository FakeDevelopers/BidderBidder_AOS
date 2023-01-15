package com.fakedevelopers.data.service

import com.fakedevelopers.domain.model.ProductItem
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductListService {
    @GET("product/getInfiniteProductList")
    suspend fun getProductList(
        @Query("searchWord") searchWord: String?,
        @Query("searchType") searchType: Int?,
        @Query("listCount") listCount: Int,
        @Query("startNumber") startNumber: Long
    ): List<ProductItem>
}
