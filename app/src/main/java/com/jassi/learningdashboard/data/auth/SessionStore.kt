package com.jassi.learningdashboard.data.auth

interface SessionStore {
    fun save(response: LoginResponse)
    fun read(): Session?
    fun clear()
}

data class Session(
    val accessToken: String,
    val email: String,
    val displayName: String,
    val expiresAtMillis: Long,
)
