package com.jassi.learningdashboard.data.auth

interface AuthApi {
    suspend fun login(email: String, password: String): LoginResponse
}

data class LoginResponse(
    val accessToken: String,
    val email: String,
    val displayName: String,
    val expiresAtMillis: Long,
)
