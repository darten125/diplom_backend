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
            val professors = Professors.fetchAll().map {
                GetAllProfessorsResponse(
                    name = it.name,
                    position = it.position,
                    department = it.department
                )
            }
            call.respond(HttpStatusCode.OK, professors)
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

        // Ищем преподавателя по имени, должности и кафедре
        val professorDTO = Professors.fetch(receive.name, receive.position, receive.department)
        if (professorDTO == null) {
            call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            return
        }
        val professorId = professorDTO.id
        try {
            // 1. Удаляем все записи из ArticleAuthors для этого преподавателя.
            ArticleAuthors.deleteByProfessor(professorId)

            // 2. Обрабатываем текущие дипломные работы (CurrentTheses):
            //    Для каждой записи, где указан этот преподаватель, обновляем пользователя, сбрасывая current_thesis_id в null,
            //    и удаляем запись из CurrentTheses.
            val currentTheses = CurrentTheses.fetchByProfessor(professorId)
            currentTheses.forEach { thesis ->
                Users.updateCurrentThesis(thesis.studentId, null)
                CurrentTheses.deleteById(thesis.id)
            }

            // 3. Удаляем все записи из PendingSupervisionRequests для этого преподавателя.
            transaction {
                PendingSupervisionRequests.deleteWhere { PendingSupervisionRequests.professorId eq professorId }
            }

            // 4. Удаляем все записи из ProcessedRequests для этого преподавателя.
            transaction {
                ProcessedRequests.deleteWhere { ProcessedRequests.professorId eq professorId }
            }

            // 5. Удаляем преподавателя из таблицы Professors.
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