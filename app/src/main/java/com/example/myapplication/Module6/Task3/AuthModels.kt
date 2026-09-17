package com.example.myapplication.Module6.Task3

import com.google.gson.annotations.SerializedName

// --- Запросы ---
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// --- Ответы (DTO) ---
data class LoginResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("token") val token: String?
) {
    val jwtToken: String get() = accessToken ?: token ?: ""
}

data class UsersResponseDto(
    @SerializedName("users") val users: List<UserDto>?
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("image") val image: String?
)

// --- Domain модели ---
data class User(
    val id: Int,
    val fullName: String,
    val username: String,
    val email: String,
    val avatarUrl: String
)

fun UserDto.toDomain() = User(
    id = id,
    fullName = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifEmpty { "Пользователь" },
    username = username ?: "user",
    email = email ?: "no-email@dummyjson.com",
    avatarUrl = image ?: "https://dummyjson.com/icon/emilys/128"
)