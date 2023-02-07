package com.fakedevelopers.domain.repository

import com.fakedevelopers.domain.model.ProductWriteDto

interface LocalStorageRepository {
    suspend fun getSearchHistory(): List<String>
    suspend fun setSearchHistory(searchHistory: List<String>): Boolean
    suspend fun getProductWrite(): ProductWriteDto
    suspend fun setProductWrite(productWriteDto: ProductWriteDto): Boolean
}
