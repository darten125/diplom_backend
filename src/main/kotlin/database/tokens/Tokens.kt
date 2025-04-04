package com.example.database.tokens

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Tokens: Table("tokens") {
    val id = uuid("id")
    private val email = varchar("email", 40)
    private val token = varchar("token", 50)

    fun insert(tokenDTO: TokenDTO){
        transaction {
            Tokens.insert{
                it[id] = tokenDTO.id
                it[email]=tokenDTO.email
                it[token]=tokenDTO.token
            }
        }
    }

    fun fetch(userToken: String): TokenDTO? {
        return try {
            transaction {
                val tokenModel = Tokens.select{ Tokens.token.eq(userToken)}.single()
                TokenDTO(
                    id = tokenModel[Tokens.id],
                    email = tokenModel[email],
                    token = tokenModel[token],
                )
            }
        } catch (e: Exception){
            null
        }
    }
}