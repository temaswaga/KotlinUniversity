package com.example.Task4.domain

import kotlinx.serialization.Serializable

@Serializable
data class Laureate(
    val id: String,
    val fullName: String,
    val portion: String,
    val motivation: String
)

@Serializable
data class NobelPrize(
    val year: String,
    val category: String,
    val laureates: List<Laureate>
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val token: String
)

@Serializable
data class ErrorResponse(
    val error: String
)