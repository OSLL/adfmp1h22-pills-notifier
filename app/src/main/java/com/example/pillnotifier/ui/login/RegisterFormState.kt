package com.example.pillnotifier.ui.login

/**
 * Data validation state of the login form.
 */
data class RegisterFormState(
    val fullNameError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)