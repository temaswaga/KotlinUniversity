package com.example.Task4.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET = "super_secret_nobel_prize_jwt_key_2026_at_least_32_chars"
    private const val ISSUER = "ktor-nobel-server"
    private const val AUDIENCE = "nobel-audience"
    const val REALM = "Nobel Prize API"

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(username: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + 30 * 60 * 1000))
            .sign(algorithm)
    }
}