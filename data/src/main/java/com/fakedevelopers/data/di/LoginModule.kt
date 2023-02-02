package com.fakedevelopers.data.di

import com.fakedevelopers.data.repository.LoginWithEmailRepositoryImpl
import com.fakedevelopers.data.repository.LoginWithSocialRepositoryImpl
import com.fakedevelopers.data.repository.RegistrationTermRepositoryImpl
import com.fakedevelopers.data.service.LoginWithEmailService
import com.fakedevelopers.data.service.LoginWithSocialService
import com.fakedevelopers.data.service.RegistrationTermService
import com.fakedevelopers.domain.repository.LoginWithEmailRepository
import com.fakedevelopers.domain.repository.LoginWithSocialRepository
import com.fakedevelopers.domain.repository.RegistrationTermRepository
import com.google.firebase.auth.FirebaseAuth
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
    fun provideLoginWithEmailService(@DataObject retrofit: Retrofit): LoginWithEmailService =
        retrofit.create(LoginWithEmailService::class.java)

    @Singleton
    @Provides
    fun provideLoginWithEmailRepository(service: LoginWithEmailService): LoginWithEmailRepository =
        LoginWithEmailRepositoryImpl(service)

    @Singleton
    @Provides
    fun provideLoginWithSocialService(@AuthDataObject retrofit: Retrofit): LoginWithSocialService =
        retrofit.create(LoginWithSocialService::class.java)

    @Singleton
    @Provides
    fun provideLoginWithSocialRepository(
        service: LoginWithSocialService,
        @DataObject auth: FirebaseAuth
    ): LoginWithSocialRepository = LoginWithSocialRepositoryImpl(service, auth)

    @Singleton
    @Provides
    fun provideTermsService(@DataObject retrofit: Retrofit): RegistrationTermService =
        retrofit.create(RegistrationTermService::class.java)

    @Singleton
    @Provides
    fun provideTermsRepository(service: RegistrationTermService): RegistrationTermRepository =
        RegistrationTermRepositoryImpl(service)
}
