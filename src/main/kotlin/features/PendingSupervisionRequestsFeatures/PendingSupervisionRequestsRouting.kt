package com.example.features.PendingSupervisionRequestsFeatures

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.get

fun Application.configurePendingSupervisionRequestsRouting() {
    routing {
        post("/create-pending-request") {
            val controller = PendingSupervisionRequestController(call)
            controller.createPendingRequest()
        }
        get("/get-pending-requests-list") {
            val controller = PendingSupervisionRequestController(call)
            controller.getPendingRequestsList()
        }

        post("/get-user-pending-requests-list") {
            val controller = PendingSupervisionRequestController(call)
            controller.getUserPendingRequestsList()
        }
    }
}