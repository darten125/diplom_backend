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
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.http.HttpHeaders
import io.ktor.server.cio.CIO


fun main() {
    val databaseUrl = System.getenv("DATABASE_URL")
        ?: error("DATABASE_URL is not set in environment variables")

    val regex = Regex("""postgres(?:ql)?://(.*?):(.*?)@(.*?):(\d+)/(.*)""")
    val matchResult = regex.matchEntire(databaseUrl)
        ?: error("Invalid DATABASE_URL format: $databaseUrl")

    val (user, password, host, port, dbname) = matchResult.destructured

    val jdbcUrl = "jdbc:postgresql://$host:$port/$dbname"

    Database.connect(
        url = jdbcUrl,
        driver = "org.postgresql.Driver",
        user = user,
        password = password
    )

    embeddedServer(io.ktor.server.cio.CIO, port = System.getenv("PORT")?.toInt() ?: 8080, host = "0.0.0.0") {

        install(CORS) {
            anyHost()
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
            allowCredentials = true
            allowNonSimpleContentTypes = true
        }

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

