package com.jassi.learningdashboard.di

import com.jassi.learningdashboard.data.auth.AuthApi
import com.jassi.learningdashboard.data.auth.KeystoreSessionStore
import com.jassi.learningdashboard.data.auth.MockAuthApi
import com.jassi.learningdashboard.data.auth.SessionStore
import com.jassi.learningdashboard.data.repository.AuthRepositoryImpl
import com.jassi.learningdashboard.data.repository.CourseRepositoryImpl
import com.jassi.learningdashboard.domain.repository.AuthRepository
import com.jassi.learningdashboard.domain.repository.CourseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun courseRepository(impl: CourseRepositoryImpl): CourseRepository

    @Binds
    @Singleton
    fun authRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    fun sessionStore(impl: KeystoreSessionStore): SessionStore

    @Binds
    @Singleton
    fun authApi(impl: MockAuthApi): AuthApi
}
