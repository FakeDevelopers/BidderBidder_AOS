package com.fakedevelopers.presentation.api.di

import com.fakedevelopers.presentation.api.repository.ChatRepository
import com.fakedevelopers.presentation.api.repository.ProductCategoryRepository
import com.fakedevelopers.presentation.api.repository.ProductDetailRepository
import com.fakedevelopers.presentation.api.repository.ProductEditRepository
import com.fakedevelopers.presentation.api.repository.ProductRegistrationRepository
import com.fakedevelopers.presentation.api.repository.ProductSearchRepository
import com.fakedevelopers.presentation.api.service.ChatService
import com.fakedevelopers.presentation.api.service.ProductCategoryService
import com.fakedevelopers.presentation.api.service.ProductDetailService
import com.fakedevelopers.presentation.api.service.ProductEditService
import com.fakedevelopers.presentation.api.service.ProductRegistrationService
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
    // 게시글 등록 요청
    @ActivityRetainedScoped
    @Provides
    fun provideUserProductRegistrationService(@AuthRetrofit retrofit: Retrofit): ProductRegistrationService =
        retrofit.create(ProductRegistrationService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideUserProductRegistrationRepository(service: ProductRegistrationService): ProductRegistrationRepository =
        ProductRegistrationRepository(service)

    // 게시글 수정 요청
    @ActivityRetainedScoped
    @Provides
    fun provideUserProductEditService(@AuthRetrofit retrofit: Retrofit): ProductEditService =
        retrofit.create(ProductEditService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideUserProductEditRepository(service: ProductEditService): ProductEditRepository =
        ProductEditRepository(service)

    // 상품 카테고리 요청
    @ActivityRetainedScoped
    @Provides
    fun provideProductCategoryService(@NormalRetrofit retrofit: Retrofit): ProductCategoryService =
        retrofit.create(ProductCategoryService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideProductCategoryRepository(service: ProductCategoryService): ProductCategoryRepository =
        ProductCategoryRepository(service)

    // 상품 상세 정보, 입찰
    @ActivityRetainedScoped
    @Provides
    fun provideProductDetailService(@NormalRetrofit retrofit: Retrofit): ProductDetailService =
        retrofit.create(ProductDetailService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideProductDetailRepository(service: ProductDetailService): ProductDetailRepository =
        ProductDetailRepository(service)

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
