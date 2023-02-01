package com.fakedevelopers.data.repository

import com.fakedevelopers.data.source.LocalStorageDataSource
import com.fakedevelopers.domain.repository.LocalStorageRepository
import javax.inject.Inject

class LocalStorageRepositoryImpl @Inject constructor(
    private val localStorageDataSource: LocalStorageDataSource
) : LocalStorageRepository {
    override suspend fun getSearchHistory(): List<String> =
        localStorageDataSource.getSearchHistory()

    override suspend fun setSearchHistory(searchHistory: List<String>) =
        localStorageDataSource.setSearchHistory(searchHistory)
}
