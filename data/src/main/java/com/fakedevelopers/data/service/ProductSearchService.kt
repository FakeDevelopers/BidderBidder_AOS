package com.fakedevelopers.data.service

import retrofit2.http.GET
import retrofit2.http.Query

interface ProductSearchService {
    @GET("product/getSearchRank")
    suspend fun getProductSearchRank(
        @Query("listCount") listCount: Int
    ): List<String>
}
