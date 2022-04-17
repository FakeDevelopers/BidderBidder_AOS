package com.fakedevelopers.bidderbidder.api.di

import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.bidderbidder.api.repository.UserLoginRepository
import com.fakedevelopers.bidderbidder.api.repository.UserProductRegistrationRepository
import com.fakedevelopers.bidderbidder.api.service.UserLoginService
import com.fakedevelopers.bidderbidder.api.service.UserProductRegistrationService
import com.orhanobut.logger.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    fun provideOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit{
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    // 로그인 요청
    @Singleton
    @Provides
    fun provideUserLoginService(retrofit: Retrofit): UserLoginService = retrofit.create(UserLoginService::class.java)

    @Singleton
    @Provides
    fun provideUserLoginRepository(service: UserLoginService): UserLoginRepository = UserLoginRepository(service)

    // 게시글 등록 요청
    @Singleton
    @Provides
    fun provideUserProductRegistrationService(retrofit: Retrofit): UserProductRegistrationService = retrofit.create(UserProductRegistrationService::class.java)

    @Singleton
    @Provides
    fun provideUserProductRegistrationRepository(service: UserProductRegistrationService): UserProductRegistrationRepository = UserProductRegistrationRepository(service)
}
