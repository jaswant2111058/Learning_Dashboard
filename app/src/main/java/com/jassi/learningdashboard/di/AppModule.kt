package com.jassi.learningdashboard.di

import android.content.Context
import android.content.res.AssetManager
import com.jassi.learningdashboard.core.network.AndroidNetworkMonitor
import com.jassi.learningdashboard.core.network.NetworkMonitor
import com.jassi.learningdashboard.core.time.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun timeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }

    @Provides
    fun assetManager(@ApplicationContext context: Context): AssetManager = context.assets

    @Provides
    fun context(@ApplicationContext context: Context): Context = context
}

@Module
@InstallIn(SingletonComponent::class)
interface AppBindings {

    @Binds
    @Singleton
    fun networkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor
}
