package com.example.features.getPreviousTheses

import com.example.features.professorsFeatures.getPreviousTheses.GetPreviousThesesController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configurePreviousThesesRouting() {
    routing {
        post("/get-previous-theses") {
            val controller = GetPreviousThesesController(call)
            controller.getPreviousTheses()
        }

        post("/push-previous-theses") {
            val controller = GetPreviousThesesController(call)
            controller.pushPreviousTheses()
        }
    }
}