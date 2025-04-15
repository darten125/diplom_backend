package com.example.features.professorsFeatures

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.get

fun Application.configureProfessorsRouting() {
    routing {
        post("/get-all-professors") {
            val controller = ProfessorsController(call)
            controller.getAllProfessors()
        }

        post("/update-professor") {
            val controller = ProfessorsController(call)
            controller.updateProfessor()
        }

        post("/delete-professor") {
            val controller = ProfessorsController(call)
            controller.deleteProfessor()
        }
    }
}