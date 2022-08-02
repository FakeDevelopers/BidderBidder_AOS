package com.fakedevelopers.bidderbidder.api.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

class DatastoreSetting {
    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val TOKEN_HISTORY_KEY = "token_history"

        val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")

        val SEARCH_HISTORY = stringSetPreferencesKey(SEARCH_HISTORY_KEY)
        val TOKEN_HISTORY = stringPreferencesKey(TOKEN_HISTORY_KEY)
    }
}
