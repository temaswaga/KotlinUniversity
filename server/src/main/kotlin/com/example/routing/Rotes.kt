package com.example.Task4.routing

import com.example.Task4.data.NobelRepository
import com.example.Task4.domain.ErrorResponse
import com.example.Task4.domain.LoginRequest
import com.example.Task4.domain.TokenResponse
import com.example.Task4.security.JwtConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(repository: NobelRepository) {
    routing {
        get("/") {
            call.respondText("Nobel Prize API Server is running!")
        }

        post("/auth/login") {
            val request = runCatching { call.receive<LoginRequest>() }.getOrNull()
            if (request == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Некорректное тело запроса"))
                return@post
            }

            if (request.username == "admin" && request.password == "123") {
                val token = JwtConfig.generateToken(request.username)
                call.respond(HttpStatusCode.OK, TokenResponse(token = token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Неверный логин или пароль"))
            }
        }

        authenticate("auth-jwt") {
            route("/prizes") {
                get {
                    call.respond(repository.getAllPrizes())
                }

                get("{year}/{category}") {
                    val year = call.parameters["year"].orEmpty()
                    val category = call.parameters["category"].orEmpty()
                    val prize = repository.getPrize(year, category)
                    if (prize != null) {
                        call.respond(prize)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Премия не найдена"))
                    }
                }

                get("{year}/{category}/laureates") {
                    val year = call.parameters["year"].orEmpty()
                    val category = call.parameters["category"].orEmpty()
                    val prize = repository.getPrize(year, category)
                    if (prize != null) {
                        call.respond(prize.laureates)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Лауреаты не найдены"))
                    }
                }
            }
        }
    }
}