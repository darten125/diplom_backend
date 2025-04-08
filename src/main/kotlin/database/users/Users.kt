package com.example.database.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object Users: Table("users") {
    val id = uuid("id")
    private val name = varchar("name",45)
    private val email = varchar("email", 40)
    private val password = varchar("password", 30)
    private val role = text("role")
    private val currentThesisId = uuid("current_thesis_id").nullable()
    private val userGroup = varchar("user_group",10)

    fun insert(userDTO: UserDTO){
        transaction {
            Users.insert{
                it[id] = userDTO.id // Генерируем UUID
                it[name]=userDTO.name
                it[email]=userDTO.email
                it[password]=userDTO.password
                it[role]=userDTO.role
                it[currentThesisId] = userDTO.currentThesisId
                it[userGroup] = userDTO.userGroup
            }
        }
    }

    fun fetch(userEmail: String): UserDTO? {
        return try {
            transaction {
                val userModel = Users.select{ Users.email.eq(userEmail)}.single()
                UserDTO(
                    id = userModel[Users.id],
                    name = userModel[name],
                    email = userModel[email],
                    password = userModel[password],
                    role = userModel[role],
                    currentThesisId = userModel[currentThesisId],
                    userGroup = userModel[userGroup]
                )
            }
        } catch (e: Exception){
            null
        }
    }

    fun fetchById(userId: UUID): UserDTO? = transaction {
        Users.select { Users.id eq userId }
            .map {
                UserDTO(
                    id = it[Users.id],
                    name = it[Users.name],
                    email = it[Users.email],
                    password = it[Users.password],
                    role = it[Users.role],
                    currentThesisId = it[Users.currentThesisId],
                    userGroup = it[Users.userGroup]
                )
            }
            .singleOrNull()
    }

    fun updateCurrentThesis(userId: UUID, thesisId: UUID?) {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[currentThesisId] = thesisId
            }
        }
    }

    fun getNameById(userId: UUID): String? {
        return transaction {
            Users.slice(name)
                .select { Users.id eq userId }
                .map { it[name] }
                .singleOrNull()
        }
    }

    fun getUserGroupById(userId: UUID): String? {
        return transaction {
            Users.slice(userGroup)
                .select { Users.id eq userId }
                .map { it[userGroup] }
                .singleOrNull()
        }
    }
}