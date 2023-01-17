package com.fakedevelopers.data.di

import com.fakedevelopers.data.repository.LoginWithEmailRepositoryImpl
import com.fakedevelopers.data.service.LoginWithEmailService
import com.fakedevelopers.domain.repository.LoginWithEmailRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {

    @Singleton
    @Provides
    fun provideLoginService(@DataObject retrofit: Retrofit): LoginWithEmailService =
        retrofit.create(LoginWithEmailService::class.java)

    @Singleton
    @Provides
    fun provideLoginRepository(service: LoginWithEmailService): LoginWithEmailRepository =
        LoginWithEmailRepositoryImpl(service)
}
