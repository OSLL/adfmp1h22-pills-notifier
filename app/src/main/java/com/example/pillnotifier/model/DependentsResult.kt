package com.example.pillnotifier.model

data class DependentsResult(
    val success: MutableList<DependentWithTakes>? = null,
    val error: String? = null
)