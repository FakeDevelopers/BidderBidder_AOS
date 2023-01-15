package com.fakedevelopers.data.di

import com.fakedevelopers.data.repository.ProductListRepositoryImpl
import com.fakedevelopers.data.service.ProductListService
import com.fakedevelopers.domain.repository.ProductListRepository
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
    fun provideProductListService(@DataObject retrofit: Retrofit): ProductListService =
        retrofit.create(ProductListService::class.java)

    @Singleton
    @Provides
    fun provideProductListRepository(service: ProductListService): ProductListRepository =
        ProductListRepositoryImpl(service)
}
