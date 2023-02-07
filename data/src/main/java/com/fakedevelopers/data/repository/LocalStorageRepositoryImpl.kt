package com.fakedevelopers.data.repository

import com.fakedevelopers.data.source.LocalStorageDataSource
import com.fakedevelopers.domain.model.ProductWriteDto
import com.fakedevelopers.domain.repository.LocalStorageRepository
import javax.inject.Inject

class LocalStorageRepositoryImpl @Inject constructor(
    private val localStorageDataSource: LocalStorageDataSource
) : LocalStorageRepository {
    override suspend fun getSearchHistory(): List<String> =
        localStorageDataSource.getSearchHistory()

    override suspend fun setSearchHistory(searchHistory: List<String>) =
        localStorageDataSource.setSearchHistory(searchHistory)

    override suspend fun getProductWrite(): ProductWriteDto {
        val productWrite = localStorageDataSource.getProductWrite()
        return ProductWriteDto(
            title = productWrite.title,
            hopePrice = productWrite.hopePrice,
            openingBid = productWrite.openingBid,
            tick = productWrite.tick,
            expiration = productWrite.expiration,
            content = productWrite.content,
            categoryId = productWrite.categoryId
        )
    }

    override suspend fun setProductWrite(productWriteDto: ProductWriteDto) =
        localStorageDataSource.setProductWrite(productWriteDto)

    override suspend fun clearProductWrite() =
        localStorageDataSource.clearProductWrite()
}
