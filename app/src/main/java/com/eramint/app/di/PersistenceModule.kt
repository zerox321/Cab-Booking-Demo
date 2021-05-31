package com.eramint.app.di

import android.content.Context
import com.eramint.app.BuildConfig
import com.eramint.app.local.DataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore = DataStore(
        context = context,
        name = BuildConfig.APPLICATION_ID
    )




}
