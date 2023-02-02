package com.fakedevelopers.presentation.api.di

import com.fakedevelopers.presentation.api.repository.ChatRepository
import com.fakedevelopers.presentation.api.repository.ProductCategoryRepository
import com.fakedevelopers.presentation.api.repository.ProductSearchRepository
import com.fakedevelopers.presentation.api.service.ChatService
import com.fakedevelopers.presentation.api.service.ProductCategoryService
import com.fakedevelopers.presentation.api.service.ProductSearchService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class MainModule {
    // 상품 카테고리 요청
    @ActivityRetainedScoped
    @Provides
    fun provideProductCategoryService(@NormalRetrofit retrofit: Retrofit): ProductCategoryService =
        retrofit.create(ProductCategoryService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideProductCategoryRepository(service: ProductCategoryService): ProductCategoryRepository =
        ProductCategoryRepository(service)

    // 스트림 유저 토큰
    @ActivityRetainedScoped
    @Provides
    fun provideChatService(@NormalRetrofit retrofit: Retrofit): ChatService =
        retrofit.create(ChatService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideChatRepository(service: ChatService): ChatRepository =
        ChatRepository(service)

    // 인기 검색어 요청
    @ActivityRetainedScoped
    @Provides
    fun provideProductSearchService(@NormalRetrofit retrofit: Retrofit): ProductSearchService =
        retrofit.create(ProductSearchService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideProductSearchRepository(service: ProductSearchService): ProductSearchRepository =
        ProductSearchRepository(service)
}
