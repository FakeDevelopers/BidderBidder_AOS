package com.fakedevelopers.bidderbidder.api.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

class DatastoreSetting {
    companion object {
        val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "search_history")
        val SEARCH_HISTORY = stringSetPreferencesKey("search_history")
    }
}
