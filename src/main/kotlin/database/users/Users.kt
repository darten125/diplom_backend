package com.example.database.users

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Users: Table("users") {
    val id = uuid("id")
    private val name = varchar("name",45)
    private val email = varchar("email", 40)
    private val password = varchar("password", 30)
    private val role = text("role")
    private val currentThesisId = uuid("current_thesis_id").nullable()

    fun insert(userDTO: UserDTO){
        transaction {
            Users.insert{
                it[id] = userDTO.id // Генерируем UUID
                it[name]=userDTO.name
                it[email]=userDTO.email
                it[password]=userDTO.password
                it[role]=userDTO.role
                it[currentThesisId] = userDTO.currentThesisId
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
                    currentThesisId = userModel[currentThesisId]
                )
            }
        } catch (e: Exception){
            null
        }
    }
}