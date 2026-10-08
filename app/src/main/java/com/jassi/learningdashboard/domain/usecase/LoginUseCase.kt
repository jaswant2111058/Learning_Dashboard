package com.jassi.learningdashboard.domain.usecase

import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> =
        authRepository.login(email.trim(), password)
}
