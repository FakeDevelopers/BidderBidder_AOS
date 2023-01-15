package com.fakedevelopers.data.di

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
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataObject

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @DataObject
    @Provides
    fun provideBaseUrl() = "http://bidderbidderapi.kro.kr:8080"

    @DataObject
    @Singleton
    @Provides
    fun provideNormalOkHttpClient() = if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder().build()
    }

    @DataObject
    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @DataObject
    @Singleton
    @Provides
    fun provideNormalRetrofit(
        @DataObject okHttpClient: OkHttpClient,
        @DataObject gson: Gson,
        @DataObject baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
