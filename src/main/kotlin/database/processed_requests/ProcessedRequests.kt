package com.example.database.processed_requests

import com.example.database.professors.Professors
import com.example.database.users.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ProcessedRequests : Table("processed_requests") {
    val id = uuid("id")
    val studentId = uuid("student_id").references(Users.id)
    val professorId = uuid("professor_id").references(Professors.id)
    val thesisTitle = text("thesis_title")
    val description = text("description")
    val accepted = bool("accepted")

    fun insert(dto: ProcessedRequestDTO) {
        transaction {
            insert {
                it[id] = dto.id
                it[studentId] = dto.studentId
                it[professorId] = dto.professorId
                it[thesisTitle] = dto.thesisTitle
                it[description] = dto.description
                it[accepted] = dto.accepted
            }
        }
    }

    fun fetchById(requestId: UUID): ProcessedRequestDTO? = transaction {
        select { ProcessedRequests.id eq requestId }
            .map {
                ProcessedRequestDTO(
                    id = it[ProcessedRequests.id],
                    studentId = it[ProcessedRequests.studentId],
                    professorId = it[ProcessedRequests.professorId],
                    thesisTitle = it[ProcessedRequests.thesisTitle],
                    description = it[ProcessedRequests.description],
                    accepted = it[ProcessedRequests.accepted]
                )
            }
            .singleOrNull()
    }

    fun fetchByStudent(studentId: UUID): List<ProcessedRequestDTO> = transaction {
        select { ProcessedRequests.studentId eq studentId }.map {
            ProcessedRequestDTO(
                id = it[ProcessedRequests.id],
                studentId = it[ProcessedRequests.studentId],
                professorId = it[ProcessedRequests.professorId],
                thesisTitle = it[ProcessedRequests.thesisTitle],
                description = it[ProcessedRequests.description],
                accepted = it[ProcessedRequests.accepted]
            )
        }
    }

    fun fetchByProfessor(professorIdParam: UUID): List<ProcessedRequestDTO> {
        return transaction {
            select { ProcessedRequests.professorId eq professorIdParam }
                .map {
                    ProcessedRequestDTO(
                        id = it[ProcessedRequests.id],
                        studentId = it[studentId],
                        professorId = it[professorId],
                        thesisTitle = it[thesisTitle],
                        description = it[description],
                        accepted = it[accepted]
                    )
                }
        }
    }

    fun deleteById(requestId: UUID): Boolean {
        return transaction {
            deleteWhere { ProcessedRequests.id eq requestId } > 0
        }
    }

    // Метод для удаления всех записей по studentId
    fun deleteByStudent(studentIdValue: UUID): Int = transaction {
        deleteWhere { studentId eq studentIdValue }
    }
}