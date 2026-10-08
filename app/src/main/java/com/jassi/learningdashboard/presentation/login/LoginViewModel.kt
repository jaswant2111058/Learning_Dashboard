package com.jassi.learningdashboard.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.domain.usecase.LoginUseCase
import com.jassi.learningdashboard.domain.validation.CredentialValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<LoginEvent> = _events

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = null, submitError = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = null, submitError = null)
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isSubmitting) return

        val validation = CredentialValidator.validate(current.email, current.password)
        if (!validation.isValid) {
            _uiState.update {
                it.copy(
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                    submitError = null,
                )
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            when (val result = login(current.email, current.password)) {
                is AppResult.Success -> {
                    _events.tryEmit(LoginEvent.LoggedIn)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, submitError = result.error)
                }
            }
        }
    }

    fun onErrorDismissed() = _uiState.update { it.copy(submitError = null) }
}
