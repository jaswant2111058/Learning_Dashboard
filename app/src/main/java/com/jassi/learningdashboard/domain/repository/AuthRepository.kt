package com.jassi.learningdashboard.domain.repository

import com.jassi.learningdashboard.core.result.AppResult

interface AuthRepository {

    fun hasActiveSession(): Boolean

    suspend fun login(email: String, password: String): AppResult<Unit>

    suspend fun logout()
}
