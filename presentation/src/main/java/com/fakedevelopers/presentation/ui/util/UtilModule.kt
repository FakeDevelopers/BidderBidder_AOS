package com.fakedevelopers.presentation.ui.util

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {

    @Singleton
    @Provides
    fun provideAlbumImageUtil(@ApplicationContext context: Context) =
        AlbumImageUtils(context.contentResolver)

    @Singleton
    @Provides
    fun provideDateUtil(@ApplicationContext context: Context) = DateUtil(context)
}
