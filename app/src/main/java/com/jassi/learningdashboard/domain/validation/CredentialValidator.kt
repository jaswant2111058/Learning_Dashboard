package com.jassi.learningdashboard.domain.validation

enum class EmailError { Blank, Malformed }

enum class PasswordError { Blank, TooShort }

data class CredentialValidation(
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
) {
    val isValid: Boolean get() = emailError == null && passwordError == null
}

object CredentialValidator {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")

    fun validate(email: String, password: String) = CredentialValidation(
        emailError = validateEmail(email),
        passwordError = validatePassword(password),
    )

    private fun validateEmail(email: String): EmailError? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> EmailError.Blank
            !EMAIL_REGEX.matches(trimmed) -> EmailError.Malformed
            else -> null
        }
    }

    private fun validatePassword(password: String): PasswordError? = when {
        password.isEmpty() -> PasswordError.Blank
        password.length < MIN_PASSWORD_LENGTH -> PasswordError.TooShort
        else -> null
    }
}
