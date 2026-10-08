package com.jassi.learningdashboard.data.auth

import com.jassi.learningdashboard.core.network.NetworkMonitor
import com.jassi.learningdashboard.core.time.TimeProvider
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class MockAuthApi @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val timeProvider: TimeProvider,
) : AuthApi {

    override suspend fun login(email: String, password: String): LoginResponse {
        delay(SIMULATED_LATENCY_MILLIS)

        if (!networkMonitor.isOnline()) {
            throw IOException("Mock auth: device is offline")
        }

        val account = ACCOUNTS[email.lowercase()]
        if (account == null || account.password != password) {
            throw AuthException(code = 401, message = "Invalid email or password")
        }

        return LoginResponse(
            accessToken = UUID.randomUUID().toString(),
            email = email.lowercase(),
            displayName = account.displayName,
            expiresAtMillis = timeProvider.nowMillis() + SESSION_LIFETIME.inWholeMilliseconds,
        )
    }

    private data class Account(val password: String, val displayName: String)

    companion object {
        const val DEMO_EMAIL = "learner@jassi.app"
        const val DEMO_PASSWORD = "password123"

        private const val SIMULATED_LATENCY_MILLIS = 1_200L
        private val SESSION_LIFETIME = 12.hours

        private val ACCOUNTS = mapOf(
            DEMO_EMAIL to Account(DEMO_PASSWORD, "Jassi Learner"),
        )
    }
}
