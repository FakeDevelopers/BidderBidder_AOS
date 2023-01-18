package com.fakedevelopers.presentation.api.di

import com.fakedevelopers.presentation.api.repository.RegistrationTermRepository
import com.fakedevelopers.presentation.api.service.RegistrationTermService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class LoginModule {
    @ViewModelScoped
    @Provides
    fun provideTermsService(@NormalRetrofit retrofit: Retrofit): RegistrationTermService =
        retrofit.create(RegistrationTermService::class.java)

    @ViewModelScoped
    @Provides
    fun provideTermsRepository(service: RegistrationTermService): RegistrationTermRepository =
        RegistrationTermRepository(service)
}
