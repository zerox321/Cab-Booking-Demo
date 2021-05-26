package com.eramint.locationservice.di

import android.content.Context
import com.eramint.locationservice.BuildConfig
import com.eramint.locationservice.local.DataStore
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
