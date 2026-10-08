package com.jassi.learningdashboard.presentation.login

import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.domain.validation.EmailError
import com.jassi.learningdashboard.domain.validation.PasswordError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val isSubmitting: Boolean = false,
    val submitError: DataError? = null,
) {
    val isInputEnabled: Boolean get() = !isSubmitting
}

sealed interface LoginEvent {
    data object LoggedIn : LoginEvent
}
