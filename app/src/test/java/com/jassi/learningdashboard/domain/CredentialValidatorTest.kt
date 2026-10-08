package com.jassi.learningdashboard.domain

import com.jassi.learningdashboard.domain.validation.CredentialValidator
import com.jassi.learningdashboard.domain.validation.EmailError
import com.jassi.learningdashboard.domain.validation.PasswordError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialValidatorTest {

    @Test
    fun `a well formed credential pair passes`() {
        val result = CredentialValidator.validate("learner@jassi.app", "password123")

        assertTrue(result.isValid)
        assertNull(result.emailError)
        assertNull(result.passwordError)
    }

    @Test
    fun `surrounding whitespace does not invalidate an email`() {
        val result = CredentialValidator.validate("  learner@jassi.app  ", "password123")

        assertTrue(result.isValid)
    }

    @Test
    fun `blank and malformed emails are reported differently`() {
        assertEquals(EmailError.Blank, CredentialValidator.validate("", "password123").emailError)
        assertEquals(EmailError.Blank, CredentialValidator.validate("   ", "password123").emailError)
        assertEquals(
            EmailError.Malformed,
            CredentialValidator.validate("learner@jassi", "password123").emailError,
        )
        assertEquals(
            EmailError.Malformed,
            CredentialValidator.validate("learner.jassi.app", "password123").emailError,
        )
    }

    @Test
    fun `password length is enforced and reported separately from blankness`() {
        assertEquals(
            PasswordError.Blank,
            CredentialValidator.validate("learner@jassi.app", "").passwordError,
        )
        assertEquals(
            PasswordError.TooShort,
            CredentialValidator.validate("learner@jassi.app", "pass").passwordError,
        )
    }

    @Test
    fun `both fields are reported at once so the user fixes the form in one pass`() {
        val result = CredentialValidator.validate("nope", "x")

        assertEquals(EmailError.Malformed, result.emailError)
        assertEquals(PasswordError.TooShort, result.passwordError)
    }
}
