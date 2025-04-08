package com.example.features.userFeatures

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.get

fun Application.configureUserRouting() {
    routing {
        get("/get-user-data") {
            val controller = UserController(call)
            controller.get()
        }
    }
}