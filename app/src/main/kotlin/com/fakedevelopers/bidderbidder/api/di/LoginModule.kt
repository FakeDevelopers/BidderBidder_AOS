package com.fakedevelopers.bidderbidder.api.di

import com.fakedevelopers.bidderbidder.api.repository.RegistrationTermRepository
import com.fakedevelopers.bidderbidder.api.repository.SigninGoogleRepository
import com.fakedevelopers.bidderbidder.api.repository.UserLoginRepository
import com.fakedevelopers.bidderbidder.api.service.RegistrationTermService
import com.fakedevelopers.bidderbidder.api.service.SigninGoogleService
import com.fakedevelopers.bidderbidder.api.service.UserLoginService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
class LoginModule {
    // 로그인 요청
    @ViewModelScoped
    @Provides
    fun provideUserLoginService(@NormalRetrofit retrofit: Retrofit): UserLoginService = retrofit.create(
        UserLoginService::class.java
    )

    @ViewModelScoped
    @Provides
    fun provideUserLoginRepository(service: UserLoginService): UserLoginRepository = UserLoginRepository(service)

    // 구글 로그인 요청
    @ViewModelScoped
    @Provides
    fun provideSigninGoogleService(@AuthRetrofit retrofit: Retrofit): SigninGoogleService =
        retrofit.create(SigninGoogleService::class.java)

    @ViewModelScoped
    @Provides
    fun provideSigninGoogleRepository(service: SigninGoogleService): SigninGoogleRepository =
        SigninGoogleRepository(service)

    // 약관 종류 요청
    @ViewModelScoped
    @Provides
    fun provideTermsService(@NormalRetrofit retrofit: Retrofit): RegistrationTermService =
        retrofit.create(RegistrationTermService::class.java)

    @ViewModelScoped
    @Provides
    fun provideTermsRepository(service: RegistrationTermService): RegistrationTermRepository =
        RegistrationTermRepository(service)
}
