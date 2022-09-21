package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.product_list.ProductListDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductListService {
    @GET("product/getInfiniteProductList")
    suspend fun getProductList(
        @Query("searchWord") searchWord: String?,
        @Query("searchType") searchType: Int?,
        @Query("listCount") listCount: Int,
        @Query("startNumber") startNumber: Long
    ): Response<List<ProductListDto>>
}
