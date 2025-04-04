package com.example.database.current_theses

import com.example.database.professors.Professors
import com.example.database.users.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object CurrentTheses : Table("current_theses") {
    val id = uuid("id")
    val studentId = uuid("student_id").references(Users.id)
    val professorId = uuid("professor_id").references(Professors.id)
    val title = text("title")
    val description = text("description")

    fun insert(currentThesisDTO: CurrentThesisDTO) {
        transaction {
            CurrentTheses.insert {
                it[id] = currentThesisDTO.id
                it[studentId] = currentThesisDTO.studentId
                it[professorId] = currentThesisDTO.professorId
                it[title] = currentThesisDTO.title
                it[description] = currentThesisDTO.description
            }
        }
    }

    fun fetchByStudent(studentId: UUID): CurrentThesisDTO? {
        return transaction {
            CurrentTheses
                .select { CurrentTheses.studentId eq studentId }
                .singleOrNull()
                ?.let {
                    CurrentThesisDTO(
                        id = it[CurrentTheses.id],
                        studentId = it[CurrentTheses.studentId],
                        professorId = it[CurrentTheses.professorId],
                        title = it[CurrentTheses.title],
                        description = it[CurrentTheses.description]
                    )
                }
        }
    }
}