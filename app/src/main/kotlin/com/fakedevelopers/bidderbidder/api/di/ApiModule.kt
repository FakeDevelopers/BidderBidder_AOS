package com.fakedevelopers.bidderbidder.api.di

import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.bidderbidder.api.repository.ProductListRepository
import com.fakedevelopers.bidderbidder.api.repository.ProductRegistrationRepository
import com.fakedevelopers.bidderbidder.api.repository.SigninGoogleRepository
import com.fakedevelopers.bidderbidder.api.repository.UserLoginRepository
import com.fakedevelopers.bidderbidder.api.service.ProductListService
import com.fakedevelopers.bidderbidder.api.service.ProductRegistrationService
import com.fakedevelopers.bidderbidder.api.service.SigninGoogleService
import com.fakedevelopers.bidderbidder.api.service.UserLoginService
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.orhanobut.logger.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

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
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // 파이어베이스 Auth
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance().apply {
            setLanguageCode(Locale.getDefault().language)
        }
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
    fun provideUserProductRegistrationService(retrofit: Retrofit): ProductRegistrationService =
        retrofit.create(ProductRegistrationService::class.java)

    @Singleton
    @Provides
    fun provideUserProductRegistrationRepository(service: ProductRegistrationService): ProductRegistrationRepository =
        ProductRegistrationRepository(service)

    // 상품 리스트 요청
    @Singleton
    @Provides
    fun provideProductListService(retrofit: Retrofit): ProductListService =
        retrofit.create(ProductListService::class.java)

    @Singleton
    @Provides
    fun provideProductListRepository(service: ProductListService): ProductListRepository =
        ProductListRepository(service)

    // 구글 로그인 요청
    @Singleton
    @Provides
    fun provideSiginGoogleService(retrofit: Retrofit): SigninGoogleService =
        retrofit.create(SigninGoogleService::class.java)

    @Singleton
    @Provides
    fun provideSiginGoogleRepository(service: SigninGoogleService): SigninGoogleRepository =
        SigninGoogleRepository(service)
}
