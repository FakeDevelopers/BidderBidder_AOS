package com.fakedevelopers.presentation.api.service

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductSearchService {
    @GET("product/getSearchRank")
    suspend fun getProductSearchRank(
        @Query("listCount") listCount: Int
    ): Response<List<String>>
}
