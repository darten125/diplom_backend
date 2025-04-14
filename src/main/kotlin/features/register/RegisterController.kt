package com.example.features.register

import com.example.database.tokens.TokenDTO
import com.example.database.tokens.Tokens
import com.example.database.users.UserDTO
import com.example.database.users.Users
import com.example.utils.isValidEmail
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*


class RegisterController(private val call: ApplicationCall) {

    suspend fun registerNewUser() {
        val receive = call.receive<RegisterReceiveRemote>()
        if (!receive.email.isValidEmail()){
            call.respond(HttpStatusCode.BadRequest, "Email is not valid")
            return
        }

        val userDTO = Users.fetch(receive.email)
        if (userDTO != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return
        }

        val token = UUID.randomUUID().toString()

        try {
            Users.insert(
                UserDTO(
                    id = UUID.randomUUID(),
                    name = receive.name,
                    email = receive.email,
                    password = receive.password,
                    role = receive.role,
                    currentThesisId = null,
                    userGroup = receive.userGroup
                )
            )

            Tokens.insert(
                TokenDTO(
                    id = UUID.randomUUID(),
                    email = receive.email,
                    token = token
                )
            )

            call.respond(HttpStatusCode.OK, RegisterResponseRemote(token = token))
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, "Registration error: ${e.localizedMessage}")
        }
    }
}
