package com.example.database.articles

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Articles : Table("articles") {
    val id = uuid("id")
    val title = text("title")
    val link = text("link")

    fun insert(articleDTO: ArticleDTO) {
        transaction {
            Articles.insert {
                it[id] = articleDTO.id
                it[title] = articleDTO.title
                it[link] = articleDTO.link
            }
        }
    }

    fun fetchById(articleId: UUID): ArticleDTO? {
        return transaction {
            Articles
                .select { Articles.id eq articleId }
                .singleOrNull()
                ?.let {
                    ArticleDTO(
                        id = it[Articles.id],
                        title = it[Articles.title],
                        link = it[Articles.link]
                    )
                }
        }
    }

    fun fetchByLink(link: String): ArticleDTO? {
        val normalizedLink = link.trim().lowercase()
        return transaction {
            Articles.select { Articles.link.lowerCase() eq normalizedLink }
                .singleOrNull()
                ?.let {
                    ArticleDTO(
                        id = it[Articles.id],
                        title = it[Articles.title],
                        link = it[Articles.link]
                    )
                }
        }
    }
}