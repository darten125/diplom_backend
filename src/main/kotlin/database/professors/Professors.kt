package com.example.database.professors

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Professors: Table("professors") {
    val id = uuid("id")
    private val name = varchar("name",45)
    private val position = text("position")
    private val department = text("department")

    fun insert(professorDTO: ProfessorDTO){
        transaction {
            Professors.insert{
                it[Professors.id] = professorDTO.id
                it[Professors.name]=professorDTO.name
                it[Professors.position]=professorDTO.position
                it[Professors.department]=professorDTO.department
            }
        }
    }

    fun fetch(name: String, position: String, department: String): ProfessorDTO? {
        return try {
            transaction {
                val professorModel = Professors
                    .select {
                        (Professors.name eq name) and
                                (Professors.position eq position) and
                                (Professors.department eq department)
                    }
                    .singleOrNull() // Используем singleOrNull для безопасного получения одного элемента

                professorModel?.let {
                    ProfessorDTO(
                        id = it[Professors.id],
                        name = it[Professors.name],
                        position = it[Professors.position],
                        department = it[Professors.department]
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchAll(): List<ProfessorDTO> {
        return transaction {
            Professors
                .selectAll()
                .orderBy(Professors.department to SortOrder.ASC)
                .map {
                    ProfessorDTO(
                        id = it[Professors.id],
                        name = it[Professors.name],
                        position = it[Professors.position],
                        department = it[Professors.department]
                    )
                }
        }
    }

    fun update(name: String, oldPosition: String, oldDepartment: String, newPosition: String, newDepartment: String): Boolean {
        return try {
            transaction {
                val updatedRows = Professors.update({
                    (Professors.name eq name) and
                            (Professors.position eq oldPosition) and
                            (Professors.department eq oldDepartment)
                }) {
                    it[position] = newPosition
                    it[department] = newDepartment
                }
                updatedRows > 0 // Возвращаем true, если хотя бы одна строка обновлена
            }
        } catch (e: Exception) {
            false
        }
    }

    fun fetchById(professorId: UUID): ProfessorDTO? {
        return transaction {
            Professors.select { Professors.id eq professorId }
                .map {
                    ProfessorDTO(
                        id = it[Professors.id],
                        name = it[Professors.name],
                        position = it[Professors.position],
                        department = it[Professors.department]
                    )
                }
                .singleOrNull()
        }
    }

    fun deleteById(professorId: UUID): Boolean {
        return transaction {
            deleteWhere { id eq professorId } > 0
        }
    }

}