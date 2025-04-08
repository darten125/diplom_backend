package com.example

import com.example.features.PendingSupervisionRequestsFeatures.configurePendingSupervisionRequestsRouting
import com.example.features.ProcessedRequestsFeatures.configureProcessedRequestsRouting
import com.example.features.getPreviousTheses.configurePreviousThesesRouting
import com.example.features.login.configureLoginRouting
import com.example.features.professorsFeatures.articlesFeatures.configureArticleRouting
import com.example.features.professorsFeatures.configureProfessorsRouting
import com.example.features.register.configureRegisterRouting
import com.example.features.tokensFeatures.configureTokenRouting
import com.example.features.userFeatures.configureUserRouting
import com.example.features.userFeatures.currentThesesFeatures.configureCurrentThesisRouting
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
        configureTokenRouting()
        configureArticleRouting()
        configurePreviousThesesRouting()
        configureProfessorsRouting()
        configureCurrentThesisRouting()
        configureUserRouting()
        configurePendingSupervisionRequestsRouting()
        configureProcessedRequestsRouting()
    }.start(wait = true)
}

