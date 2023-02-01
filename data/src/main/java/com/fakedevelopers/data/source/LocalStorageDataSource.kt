package com.fakedevelopers.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalStorageDataSource @Inject constructor(
    private val context: Context
) {
    private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")
    private val searchHistoryKey = stringSetPreferencesKey(name = "search_history")

    suspend fun setSearchHistory(searchHistory: List<String>): Boolean =
        runCatching {
            context.datastore.edit { preferences ->
                preferences[searchHistoryKey] = searchHistory.toSet()
            }
        }.isSuccess

    suspend fun getSearchHistory(): List<String> =
        context.datastore.data.map { preferences ->
            preferences[searchHistoryKey]?.toList() ?: emptyList()
        }.first()
}
