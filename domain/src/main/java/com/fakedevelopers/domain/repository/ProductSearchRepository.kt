package com.fakedevelopers.domain.repository

interface ProductSearchRepository {
    suspend fun getProductSearchRank(listCount: Int): Result<List<String>>
}
