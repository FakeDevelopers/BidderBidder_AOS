package com.fakedevelopers.data.di

import android.content.Context
import com.fakedevelopers.data.repository.ImageRepositoryImpl
import com.fakedevelopers.data.repository.ProductEditorRepositoryImpl
import com.fakedevelopers.data.repository.ProductListRepositoryImpl
import com.fakedevelopers.data.service.ProductEditorService
import com.fakedevelopers.data.service.ProductListService
import com.fakedevelopers.domain.repository.ImageRepository
import com.fakedevelopers.domain.repository.ProductEditorRepository
import com.fakedevelopers.domain.repository.ProductListRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Singleton
    @Provides
    fun provideProductEditorService(@AuthDataObject retrofit: Retrofit): ProductEditorService =
        retrofit.create(ProductEditorService::class.java)

    @Singleton
    @Provides
    fun provideProductEditorRepository(service: ProductEditorService): ProductEditorRepository =
        ProductEditorRepositoryImpl(service)

    @Singleton
    @Provides
    fun provideImageRepository(@ApplicationContext context: Context): ImageRepository =
        ImageRepositoryImpl(context.contentResolver)
}
