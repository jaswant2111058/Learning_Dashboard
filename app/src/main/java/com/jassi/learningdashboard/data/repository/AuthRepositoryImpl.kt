package com.jassi.learningdashboard.data.repository

import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.core.result.toDataError
import com.jassi.learningdashboard.data.auth.AuthApi
import com.jassi.learningdashboard.data.auth.SessionStore
import com.jassi.learningdashboard.di.IoDispatcher
import com.jassi.learningdashboard.domain.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    override fun hasActiveSession(): Boolean = sessionStore.read() != null

    override suspend fun login(email: String, password: String): AppResult<Unit> =
        withContext(ioDispatcher) {
            try {
                val response = authApi.login(email, password)
                sessionStore.save(response)
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                AppResult.Failure(throwable.toDataError())
            }
        }

    override suspend fun logout() {
        withContext(ioDispatcher) { sessionStore.clear() }
    }
}
