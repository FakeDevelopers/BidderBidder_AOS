package com.fakedevelopers.bidderbidder.api.service

import com.fakedevelopers.bidderbidder.ui.product_list.ProductListDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductListService {
    @GET("board/getInfiniteProductList")
    suspend fun postProductList(
        @Query("searchWord") searchWord: String?,
        @Query("listCount") listCount: Int,
        @Query("startNumber") startNumber: Long
    ): Response<List<ProductListDto>>
}
