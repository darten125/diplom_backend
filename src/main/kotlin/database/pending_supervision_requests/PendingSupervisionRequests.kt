package com.example.database.pending_supervision_requests

import com.example.database.professors.Professors
import com.example.database.users.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object PendingSupervisionRequests : Table("pending_supervision_requests") {
    val id = uuid("id")
    val studentId = uuid("student_id").references(Users.id)
    val professorId = uuid("professor_id").references(Professors.id)
    val thesisTitle = text("thesis_title")
    val description = text("description")
    val accepted = bool("accepted").nullable()

    fun insert(dto: PendingSupervisionRequestDTO) {
        transaction {
            insert {
                it[PendingSupervisionRequests.id] = dto.id
                it[PendingSupervisionRequests.studentId] = dto.studentId
                it[PendingSupervisionRequests.professorId] = dto.professorId
                it[PendingSupervisionRequests.thesisTitle] = dto.thesisTitle
                it[PendingSupervisionRequests.description] = dto.description
                it[PendingSupervisionRequests.accepted] = dto.accepted
            }
        }
    }

    fun fetchAll(): List<PendingSupervisionRequestDTO> = transaction {
        selectAll().map {
            PendingSupervisionRequestDTO(
                id = it[PendingSupervisionRequests.id],
                studentId = it[PendingSupervisionRequests.studentId],
                professorId = it[PendingSupervisionRequests.professorId],
                thesisTitle = it[PendingSupervisionRequests.thesisTitle],
                description = it[PendingSupervisionRequests.description],
                accepted = it[PendingSupervisionRequests.accepted]
            )
        }
    }

    fun fetchById(requestId: UUID): PendingSupervisionRequestDTO? = transaction {
        select { PendingSupervisionRequests.id eq requestId }
            .map {
                PendingSupervisionRequestDTO(
                    id = it[PendingSupervisionRequests.id],
                    studentId = it[PendingSupervisionRequests.studentId],
                    professorId = it[PendingSupervisionRequests.professorId],
                    thesisTitle = it[PendingSupervisionRequests.thesisTitle],
                    description = it[PendingSupervisionRequests.description],
                    accepted = it[PendingSupervisionRequests.accepted]
                )
            }
            .singleOrNull()
    }

    fun fetchByProfessor(professorId: UUID): List<PendingSupervisionRequestDTO> {
        return transaction {
            select { PendingSupervisionRequests.professorId eq professorId }
                .map {
                    PendingSupervisionRequestDTO(
                        id = it[PendingSupervisionRequests.id],
                        studentId = it[PendingSupervisionRequests.studentId],
                        professorId = it[PendingSupervisionRequests.professorId],
                        thesisTitle = it[PendingSupervisionRequests.thesisTitle],
                        description = it[PendingSupervisionRequests.description],
                        accepted = it[PendingSupervisionRequests.accepted]
                    )
                }
        }
    }

    fun fetchByStudent(studentId: UUID): List<PendingSupervisionRequestDTO> {
        return transaction {
            select { PendingSupervisionRequests.studentId eq studentId }
                .map {
                    PendingSupervisionRequestDTO(
                        id = it[PendingSupervisionRequests.id],
                        studentId = it[PendingSupervisionRequests.studentId],
                        professorId = it[PendingSupervisionRequests.professorId],
                        thesisTitle = it[PendingSupervisionRequests.thesisTitle],
                        description = it[PendingSupervisionRequests.description],
                        accepted = it[PendingSupervisionRequests.accepted]
                    )
                }
        }
    }

    fun deleteById(requestId: UUID): Boolean {
        return transaction {
            PendingSupervisionRequests.deleteWhere { id eq requestId } > 0
        }
    }
}