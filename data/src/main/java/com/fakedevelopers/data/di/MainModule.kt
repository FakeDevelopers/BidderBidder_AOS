package com.fakedevelopers.data.di

import android.content.Context
import com.fakedevelopers.data.repository.AlbumRepositoryImpl
import com.fakedevelopers.data.repository.ChatRepositoryImpl
import com.fakedevelopers.data.repository.ImageRepositoryImpl
import com.fakedevelopers.data.repository.LocalStorageRepositoryImpl
import com.fakedevelopers.data.repository.ProductCategoryRepositoryImpl
import com.fakedevelopers.data.repository.ProductDetailRepositoryImpl
import com.fakedevelopers.data.repository.ProductEditorRepositoryImpl
import com.fakedevelopers.data.repository.ProductListRepositoryImpl
import com.fakedevelopers.data.repository.ProductSearchRepositoryImpl
import com.fakedevelopers.data.service.ChatService
import com.fakedevelopers.data.service.ProductCategoryService
import com.fakedevelopers.data.service.ProductDetailService
import com.fakedevelopers.data.service.ProductEditorService
import com.fakedevelopers.data.service.ProductListService
import com.fakedevelopers.data.service.ProductSearchService
import com.fakedevelopers.data.source.LocalStorageDataSource
import com.fakedevelopers.domain.repository.AlbumRepository
import com.fakedevelopers.domain.repository.ChatRepository
import com.fakedevelopers.domain.repository.ImageRepository
import com.fakedevelopers.domain.repository.LocalStorageRepository
import com.fakedevelopers.domain.repository.ProductCategoryRepository
import com.fakedevelopers.domain.repository.ProductDetailRepository
import com.fakedevelopers.domain.repository.ProductEditorRepository
import com.fakedevelopers.domain.repository.ProductListRepository
import com.fakedevelopers.domain.repository.ProductSearchRepository
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
    fun provideProductDetailService(@DataObject retrofit: Retrofit): ProductDetailService =
        retrofit.create(ProductDetailService::class.java)

    @Singleton
    @Provides
    fun provideProductDetailRepository(service: ProductDetailService): ProductDetailRepository =
        ProductDetailRepositoryImpl(service)

    @Singleton
    @Provides
    fun provideImageRepository(@ApplicationContext context: Context): ImageRepository =
        ImageRepositoryImpl(context.contentResolver)

    @Singleton
    @Provides
    fun provideLocalStorageRepository(localStorageDataSource: LocalStorageDataSource): LocalStorageRepository =
        LocalStorageRepositoryImpl(localStorageDataSource)

    @Singleton
    @Provides
    fun provideLocalStorageDataSource(@ApplicationContext context: Context): LocalStorageDataSource =
        LocalStorageDataSource(context)

    @Singleton
    @Provides
    fun provideProductCategoryService(@DataObject retrofit: Retrofit): ProductCategoryService =
        retrofit.create(ProductCategoryService::class.java)

    @Singleton
    @Provides
    fun provideProductCategoryRepository(service: ProductCategoryService): ProductCategoryRepository =
        ProductCategoryRepositoryImpl(service)

    // 스트림 유저 토큰
    @Singleton
    @Provides
    fun provideChatService(@DataObject retrofit: Retrofit): ChatService =
        retrofit.create(ChatService::class.java)

    @Singleton
    @Provides
    fun provideChatRepository(service: ChatService): ChatRepository =
        ChatRepositoryImpl(service)

    // 인기 검색어 요청
    @Singleton
    @Provides
    fun provideProductSearchService(@DataObject retrofit: Retrofit): ProductSearchService =
        retrofit.create(ProductSearchService::class.java)

    @Singleton
    @Provides
    fun provideProductSearchRepository(service: ProductSearchService): ProductSearchRepository =
        ProductSearchRepositoryImpl(service)

    @Singleton
    @Provides
    fun provideAlbumRepository(): AlbumRepository =
        AlbumRepositoryImpl()
}
