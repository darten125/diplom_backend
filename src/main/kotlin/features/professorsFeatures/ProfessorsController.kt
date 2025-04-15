package com.example.features.professorsFeatures

import com.example.database.article_authors.ArticleAuthors
import com.example.database.current_theses.CurrentTheses
import com.example.database.pending_supervision_requests.PendingSupervisionRequests
import com.example.database.processed_requests.ProcessedRequests
import com.example.database.professors.Professors
import com.example.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ProfessorsController(private val call: ApplicationCall) {

    suspend fun getAllProfessors() {
        try {
            val professorsList = Professors.fetchAll().map { professor ->
                ProfessorsRemote(
                    id = professor.id.toString(),
                    name = professor.name,
                    position = professor.position,
                    department = professor.department
                )
            }
            call.respond(HttpStatusCode.OK, GetAllProfessorsResponse(professors = professorsList))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Ошибка при получении списка преподавателей")
        }
    }

    suspend fun updateProfessor() {
        val receive = call.receive<UpdateProfessorReceiveRemote>()
        val updated = Professors.update(
            name = receive.name,
            oldPosition = receive.oldPosition,
            oldDepartment = receive.oldDepartment,
            newPosition = receive.newPosition,
            newDepartment = receive.newDepartment
        )
        if (updated) {
            call.respond(HttpStatusCode.OK, UpdateProfessorResponseRemote("Преподаватель обновлен"))
        } else {
            call.respond(HttpStatusCode.NotFound, UpdateProfessorResponseRemote("Преподаватель не найден или данные не изменились"))
        }
    }

    suspend fun deleteProfessor() {
        val receive = call.receive<DeleteProfessorReceiveRemote>()

        val professorDTO = Professors.fetch(receive.name, receive.position, receive.department)
        if (professorDTO == null) {
            call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            return
        }
        val professorId = professorDTO.id
        try {
            ArticleAuthors.deleteByProfessor(professorId)

            val currentTheses = CurrentTheses.fetchByProfessor(professorId)
            currentTheses.forEach { thesis ->
                Users.updateCurrentThesis(thesis.studentId, null)
                CurrentTheses.deleteById(thesis.id)
            }

            transaction {
                PendingSupervisionRequests.deleteWhere { PendingSupervisionRequests.professorId eq professorId }
            }

            transaction {
                ProcessedRequests.deleteWhere { ProcessedRequests.professorId eq professorId }
            }

            val deleted = Professors.deleteById(professorId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Преподаватель и связанные данные успешно удалены")
            } else {
                call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Ошибка при удалении преподавателя: ${e.localizedMessage}")
        }
    }
}