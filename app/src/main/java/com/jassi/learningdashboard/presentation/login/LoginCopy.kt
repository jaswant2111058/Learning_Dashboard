package com.jassi.learningdashboard.presentation.login

import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.domain.validation.CredentialValidator
import com.jassi.learningdashboard.domain.validation.EmailError
import com.jassi.learningdashboard.domain.validation.PasswordError

fun EmailError.message(): String = when (this) {
    EmailError.Blank -> "Enter your email address"
    EmailError.Malformed -> "That does not look like a valid email"
}

fun PasswordError.message(): String = when (this) {
    PasswordError.Blank -> "Enter your password"
    PasswordError.TooShort ->
        "Use at least ${CredentialValidator.MIN_PASSWORD_LENGTH} characters"
}

fun DataError.loginMessage(): String = when (this) {
    DataError.Unauthorized -> "Incorrect email or password"
    DataError.Network -> "No internet connection. Check your network and try again."
    is DataError.Server -> "Our servers are having trouble (error $code). Please try again."
    DataError.Serialization -> "Unexpected response from the server. Please update the app."
    is DataError.Unknown -> "Something went wrong. Please try again."
}
