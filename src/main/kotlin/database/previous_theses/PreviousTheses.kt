package com.example.database.previous_theses

import com.example.database.professors.Professors
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object PreviousTheses : Table("previous_theses") {
    val id = uuid("id")
    val title = text("title")
    val professorId = uuid("professor_id").references(Professors.id)

    fun insert(previousThesisDTO: PreviousThesisDTO) {
        transaction {
            PreviousTheses.insert {
                it[id] = previousThesisDTO.id
                it[title] = previousThesisDTO.title
                it[professorId] = previousThesisDTO.professorId
            }
        }
    }

    fun fetchByProfessor(professorId: UUID): List<PreviousThesisDTO> {
        return transaction {
            PreviousTheses
                .select { PreviousTheses.professorId eq professorId }
                .map {
                    PreviousThesisDTO(
                        id = it[PreviousTheses.id],
                        title = it[PreviousTheses.title],
                        professorId = it[PreviousTheses.professorId]
                    )
                }
        }
    }
}