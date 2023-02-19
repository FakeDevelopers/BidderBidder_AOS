package com.fakedevelopers.data.di

import com.fakedevelopers.data.util.LoginAuthInterceptor
import com.fakedevelopers.domain.secret.Constants.Companion.BASE_URL
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
annotation class NetworkObject

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthNetworkObject

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @NetworkObject
    @Singleton
    @Provides
    fun provideNormalOkHttpClient(): OkHttpClient =
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        } else {
            OkHttpClient.Builder().build()
        }

    @NetworkObject
    @Singleton
    @Provides
    fun provideNormalRetrofit(
        @NetworkObject okHttpClient: OkHttpClient,
        @NetworkObject gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @AuthNetworkObject
    @Singleton
    @Provides
    fun provideAuthOkHttpClient(
        @NetworkObject authInterceptor: LoginAuthInterceptor
    ): OkHttpClient =
        if (BuildConfig.DEBUG.not()) {
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

    @AuthNetworkObject
    @Singleton
    @Provides
    fun provideAuthRetrofit(
        @AuthNetworkObject okHttpClient: OkHttpClient,
        @NetworkObject gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @NetworkObject
    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @NetworkObject
    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance().apply {
            setLanguageCode(Locale.getDefault().language)
        }
    }

    @NetworkObject
    @Singleton
    @Provides
    fun provideAuthInterceptor(auth: FirebaseAuth) = LoginAuthInterceptor(auth)
}
