package com.example.features.ProcessedRequestsFeatures

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post

fun Application.configureProcessedRequestsRouting() {
    routing {
        post("/push-processed_requests") {
            val controller = ProcessedRequestsController(call)
            controller.pushProcessedRequests()
        }
        get("/get-user-processed-requests") {
            val controller = ProcessedRequestsController(call)
            controller.getUserProcessedRequests()
        }
    }
}