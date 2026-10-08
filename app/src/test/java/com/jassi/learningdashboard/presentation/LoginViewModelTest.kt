package com.jassi.learningdashboard.presentation

import app.cash.turbine.test
import com.jassi.learningdashboard.MainDispatcherRule
import com.jassi.learningdashboard.core.result.AppResult
import com.jassi.learningdashboard.core.result.DataError
import com.jassi.learningdashboard.domain.usecase.LoginUseCase
import com.jassi.learningdashboard.domain.validation.EmailError
import com.jassi.learningdashboard.domain.validation.PasswordError
import com.jassi.learningdashboard.fake.FakeAuthRepository
import com.jassi.learningdashboard.presentation.login.LoginEvent
import com.jassi.learningdashboard.presentation.login.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private fun viewModel() = LoginViewModel(LoginUseCase(authRepository))

    @Test
    fun `an invalid form is rejected locally without costing a request`() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("abc")

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EmailError.Malformed, state.emailError)
        assertEquals(PasswordError.TooShort, state.passwordError)
        assertFalse(state.isSubmitting)
        assertEquals(0, authRepository.loginAttempts)
    }

    @Test
    fun `editing a field clears its error so it does not stay red while being fixed`() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange("nope")
        viewModel.onSubmit()
        assertEquals(EmailError.Malformed, viewModel.uiState.value.emailError)

        viewModel.onEmailChange("learner@jassi.app")

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `a valid submit emits a logged in event`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onEmailChange("learner@jassi.app")
            viewModel.onPasswordChange("password123")
            viewModel.onSubmit()
            advanceUntilIdle()

            assertEquals(LoginEvent.LoggedIn, awaitItem())
            assertEquals(1, authRepository.loginAttempts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `rejected credentials surface an error and re-enable the form`() = runTest {
        authRepository.loginResult = AppResult.Failure(DataError.Unauthorized)
        val viewModel = viewModel()

        viewModel.onEmailChange("learner@jassi.app")
        viewModel.onPasswordChange("wrong-password")
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DataError.Unauthorized, state.submitError)
        assertFalse(state.isSubmitting)
    }

    @Test
    fun `a double tap on log in only triggers one request`() = runTest {
        val viewModel = viewModel()
        viewModel.onEmailChange("learner@jassi.app")
        viewModel.onPasswordChange("password123")

        viewModel.onSubmit()
        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(1, authRepository.loginAttempts)
    }
}
