package com.fakedevelopers.domain.repository

interface LocalStorageRepository {
    suspend fun getSearchHistory(): List<String>
    suspend fun setSearchHistory(searchHistory: List<String>): Boolean
}
