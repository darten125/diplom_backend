package com.example.database.article_authors

import com.example.database.articles.Articles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ArticleAuthors : Table("article_authors") {
    val id = uuid("id")
    val articleId = uuid("article_id").references(Articles.id)
    val professorId = uuid("professor_id").references(com.example.database.professors.Professors.id)

    fun insert(articleAuthorDTO: ArticleAuthorDTO) {
        transaction {
            ArticleAuthors.insert {
                it[id] = articleAuthorDTO.id
                it[articleId] = articleAuthorDTO.articleId
                it[professorId] = articleAuthorDTO.professorId
            }
        }
    }

    fun fetchByArticle(articleId: UUID): List<ArticleAuthorDTO> {
        return transaction {
            ArticleAuthors
                .select { ArticleAuthors.articleId eq articleId }
                .map {
                    ArticleAuthorDTO(
                        id = it[ArticleAuthors.id],
                        articleId = it[ArticleAuthors.articleId],
                        professorId = it[ArticleAuthors.professorId]
                    )
                }
        }
    }

    fun fetchByProfessor(professorId: UUID): List<ArticleAuthorDTO> {
        return transaction {
            ArticleAuthors
                .select { ArticleAuthors.professorId eq professorId }
                .map {
                    ArticleAuthorDTO(
                        id = it[ArticleAuthors.id],
                        articleId = it[ArticleAuthors.articleId],
                        professorId = it[ArticleAuthors.professorId]
                    )
                }
        }
    }

    fun deleteByProfessor(professorIdParam: UUID): Int {
        return transaction {
            deleteWhere { ArticleAuthors.professorId eq professorIdParam }
        }
    }

    // Метод для проверки существования связи
    fun exists(articleId: UUID, professorId: UUID): Boolean = transaction {
        select { (ArticleAuthors.articleId eq articleId) and (ArticleAuthors.professorId eq professorId) }
            .any()
    }
}