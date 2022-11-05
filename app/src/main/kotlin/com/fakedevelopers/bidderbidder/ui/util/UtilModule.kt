package com.fakedevelopers.bidderbidder.ui.util

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
    fun provideContentResolverUtil(@ApplicationContext context: Context) =
        ContentResolverUtil(context.contentResolver)

    @Singleton
    @Provides
    fun provideAlbumImageUtil(@ApplicationContext context: Context) =
        AlbumImageUtils(context.contentResolver)
}
