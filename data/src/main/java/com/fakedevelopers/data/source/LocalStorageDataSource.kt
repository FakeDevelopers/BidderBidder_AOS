package com.fakedevelopers.data.source

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fakedevelopers.data.model.ProductWrite
import com.fakedevelopers.data.serializer.ProductWriteSerializer
import com.fakedevelopers.domain.model.ProductWriteDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalStorageDataSource @Inject constructor(
    private val context: Context
) {
    private val Context.productWriteDatastore: DataStore<ProductWrite> by dataStore(
        fileName = "product_write.pb",
        serializer = ProductWriteSerializer
    )
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

    suspend fun setProductWrite(productWriteDto: ProductWriteDto) =
        runCatching {
            context.productWriteDatastore.updateData { productWrite ->
                productWrite.toBuilder()
                    .setTitle(productWriteDto.title)
                    .setHopePrice(productWrite.hopePrice)
                    .setOpeningBid(productWrite.openingBid)
                    .setTick(productWrite.tick)
                    .setExpiration(productWrite.expiration)
                    .setContent(productWrite.content)
                    .setCategoryId(productWrite.categoryId)
                    .build()
            }
        }.isSuccess


    suspend fun getProductWrite(): ProductWrite =
        context.productWriteDatastore.data.first()

    suspend fun clearProductWrite() =
        runCatching {
            context.productWriteDatastore.updateData { productWrite ->
                productWrite.toBuilder().clear().build()
            }
        }.isSuccess
}
