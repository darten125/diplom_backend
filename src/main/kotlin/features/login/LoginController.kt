package com.example.features.login

import com.example.database.tokens.TokenDTO
import com.example.database.tokens.Tokens
import com.example.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID

class LoginController(private val call: ApplicationCall) {

    suspend  fun performLogin() {
        val receive = call.receive<LoginReceiveRemote>()
        val userDTO = Users.fetch(receive.email)

        if (userDTO == null) {
            call.respond(HttpStatusCode.BadRequest, "User not found")
        } else {
            if (userDTO.password == receive.password) {
                val token = UUID.randomUUID().toString()
                Tokens.insert(
                    TokenDTO(
                        id = UUID.randomUUID(),
                        email = receive.email,
                        token = token
                    )
                )
                call.respond(LoginResponseRemote(token=token))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid password")
            }
        }
    }

    suspend fun validateToken() {
        // Извлекаем заголовок Authorization (ожидаем "Bearer <token>")
        val authHeader = call.request.headers["Authorization"]
        if (authHeader.isNullOrBlank()) {
            call.respond(HttpStatusCode.Unauthorized, "No token provided")
            return
        }

        // Убираем префикс "Bearer" и очищаем пробелы
        val token = authHeader.removePrefix("Bearer").trim()
        if (token.isEmpty()) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token format")
            return
        }

        // Проверяем наличие токена в базе данных
        val tokenDTO = Tokens.fetch(token)
        if (tokenDTO == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
        } else {
            call.respond(HttpStatusCode.OK, "Token is valid")
        }
    }
}