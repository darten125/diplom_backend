package com.example.features.tokensFeatures

import com.example.database.tokens.Tokens
import com.example.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*

class TokenController(private val call: ApplicationCall) {

    suspend fun validateToken() {
        val tokenRequest = try {
            call.receive<TokenValidationReceiveRemote>()
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Invalid JSON format: ${e.localizedMessage}")
            return
        }

        val token = tokenRequest.token.trim()
        if (token.isEmpty()) {
            call.respond(HttpStatusCode.Unauthorized, "Token is empty")
            return
        }

        val tokenDTO = Tokens.fetch(token)
        if (tokenDTO == null) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid token")
        } else {
            val userDTO = Users.fetch(tokenDTO.email)
            if (userDTO == null) {
                call.respond(HttpStatusCode.NotFound, "User not found for token")
            } else {
                val response = TokenValidationResponseRemote(
                    id = userDTO.id.toString(),
                    name = userDTO.name,
                    email = userDTO.email,
                    password = userDTO.password,
                    role = userDTO.role,
                    currentThesisId = userDTO.currentThesisId?.toString(),
                    userGroup = userDTO.userGroup
                )
                call.respond(HttpStatusCode.OK, response)
            }
        }
    }

    suspend fun deleteToken() {
        val receive = call.receive<DeleteTokenReceiveRemote>()
        try {
            val tokenId = UUID.fromString(receive.id)
            val deleted = Tokens.deleteById(tokenId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Token deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "Token not found")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, "Error: ${e.localizedMessage}")
        }
    }
}