package com.example.features.userFeatures

import com.example.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*


class UserController(private val call: ApplicationCall) {
    suspend fun get() {
        val receive = call.receive<GetUserDataReceiveRemote>()
        val user = Users.fetch(receive.email)

        if (user != null) {
            call.respond(
                HttpStatusCode.OK,
                GetUserDataResponseRemote(
                    id = user.id.toString(),
                    name = user.name,
                    email = user.email,
                    password = user.password,
                    role = user.role,
                    currentThesisId = user.currentThesisId?.toString(),
                    userGroup = user.userGroup
                )
            )
        } else {
            call.respond(HttpStatusCode.NotFound, "Пользователь с указанным email не найден")
        }
    }
}