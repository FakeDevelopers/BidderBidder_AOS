package com.fakedevelopers.bidderbidder.api.di

import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.bidderbidder.api.repository.RegistrationTermRepository
import com.fakedevelopers.bidderbidder.api.service.RegistrationTermService
import com.fakedevelopers.bidderbidder.api.util.LoginAuthInterceptor
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
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NormalRetrofit

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideBaseUrl() = BASE_URL

    @Singleton
    @Provides
    @AuthOkHttpClient
    fun provideAuthOkHttpClient(authInterceptor: LoginAuthInterceptor) = if (BuildConfig.DEBUG.not()) {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Singleton
    @Provides
    @NormalOkHttpClient
    fun provideNormalOkHttpClient() = if (BuildConfig.DEBUG) {
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
    @AuthRetrofit
    fun provideAuthRetrofit(@AuthOkHttpClient okHttpClient: OkHttpClient, gson: Gson, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Singleton
    @Provides
    @NormalRetrofit
    fun provideNormalRetrofit(@NormalOkHttpClient okHttpClient: OkHttpClient, gson: Gson, baseUrl: String): Retrofit {
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

    @Singleton
    @Provides
    fun provideAuthInterceptor(auth: FirebaseAuth) = LoginAuthInterceptor(auth)

    @Singleton
    @Provides
    fun provideTermsService(@NormalRetrofit retrofit: Retrofit): RegistrationTermService =
        retrofit.create(RegistrationTermService::class.java)

    @Singleton
    @Provides
    fun provideTermsRepository(service: RegistrationTermService): RegistrationTermRepository =
        RegistrationTermRepository(service)
}
