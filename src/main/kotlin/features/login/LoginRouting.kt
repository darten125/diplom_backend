package com.example.features.login

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureLoginRouting() {
    routing {
        post("/login") {
            val loginController = LoginController(call)
            loginController.performLogin()
        }

        get("/validate-token") {
            val loginController = LoginController(call)
            loginController.validateToken()
        }
    }
}