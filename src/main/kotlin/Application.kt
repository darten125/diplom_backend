package com.example

import com.example.features.getPreviousTheses.configurePreviousThesesRouting
import com.example.features.login.configureLoginRouting
import com.example.features.professorsFeatures.articlesFeatures.configureArticleRouting
import com.example.features.register.configureRegisterRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database


fun main() {
    Database.connect(
        url = "jdbc:postgresql://localhost:5433/diplom",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "1234"
    )

    embeddedServer(io.ktor.server.cio.CIO, port = 8080, host = "0.0.0.0") {
        configureSerialization()
        configureRouting()
        configureRegisterRouting()
        configureLoginRouting()
        configureArticleRouting()
        configurePreviousThesesRouting()
    }.start(wait = true)
}

