package com.example.features.tokensFeatures

import com.example.features.login.LoginController
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.get

fun Application.configureTokenRouting() {
    routing {
        post("/validate-token") {
            val controller = TokenController(call)
            controller.validateToken()
        }
        post("/delete-token") {
            val controller = TokenController(call)
            controller.deleteToken()
        }
    }
}