package com.example.features.userFeatures.currentThesesFeatures

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureCurrentThesisRouting() {
    routing {
        post("/create-current-thesis") {
            val controller = CurrentThesesController(call)
            controller.create()
        }

        get("/get-current-thesis") {
            val controller = CurrentThesesController(call)
            controller.get()
        }

        get("/get-all-current-theses") {
            val controller = CurrentThesesController(call)
            controller.getAllCurrentThesesExcel()
        }
    }
}