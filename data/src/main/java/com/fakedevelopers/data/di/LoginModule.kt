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
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
class LoginModule {
    @ActivityRetainedScoped
    @Provides
    fun provideLoginWithEmailService(@NetworkObject retrofit: Retrofit): LoginWithEmailService =
        retrofit.create(LoginWithEmailService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideLoginWithEmailRepository(service: LoginWithEmailService): LoginWithEmailRepository =
        LoginWithEmailRepositoryImpl(service)

    @ActivityRetainedScoped
    @Provides
    fun provideLoginWithSocialService(@AuthNetworkObject retrofit: Retrofit): LoginWithSocialService =
        retrofit.create(LoginWithSocialService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideLoginWithSocialRepository(
        service: LoginWithSocialService,
        @NetworkObject auth: FirebaseAuth
    ): LoginWithSocialRepository = LoginWithSocialRepositoryImpl(service, auth)

    @ActivityRetainedScoped
    @Provides
    fun provideTermsService(@NetworkObject retrofit: Retrofit): RegistrationTermService =
        retrofit.create(RegistrationTermService::class.java)

    @ActivityRetainedScoped
    @Provides
    fun provideTermsRepository(service: RegistrationTermService): RegistrationTermRepository =
        RegistrationTermRepositoryImpl(service)
}
