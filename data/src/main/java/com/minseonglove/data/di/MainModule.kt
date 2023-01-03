package com.minseonglove.data.di

import com.minseonglove.data.repository.ProductListRepositoryImpl
import com.minseonglove.data.service.ProductListService
import com.minseonglove.domain.repository.ProductListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Singleton
    @Provides
    fun provideProductListService(retrofit: Retrofit): ProductListService =
        retrofit.create(ProductListService::class.java)

    @Singleton
    @Provides
    fun provideProductListRepository(service: ProductListService): ProductListRepository =
        ProductListRepositoryImpl(service)
}
