package com.example.features.professorsFeatures.articlesFeatures

import com.example.database.article_authors.ArticleAuthorDTO
import com.example.database.articles.ArticleDTO
import com.example.database.articles.Articles
import com.example.database.article_authors.ArticleAuthors
import com.example.database.professors.ProfessorDTO
import com.example.database.professors.Professors
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ArticleController(private val call: ApplicationCall) {

    suspend fun addNewArticle() {
        val receive = call.receive<AddNewArticleReceiveRemote>()
        try {
            val normalizedLink = receive.link.trim()
            val normalizedTitle = receive.title.trim()

            val existingArticle = Articles.fetchByLink(normalizedLink)
            val articleId = if (existingArticle != null) {
                existingArticle.id
            } else {
                val newArticleId = UUID.randomUUID()
                Articles.insert(
                    ArticleDTO(
                        id = newArticleId,
                        title = normalizedTitle,
                        link = normalizedLink
                    )
                )
                newArticleId
            }

            val duplicateAuthors = mutableListOf<String>()

            receive.authors.forEach { author ->
                val normalizedName = author.name.trim()
                val normalizedPosition = author.position.trim()
                val normalizedDepartment = author.department.trim()

                val professorDTO = Professors.fetch(normalizedName, normalizedPosition, normalizedDepartment)
                val professorId = professorDTO?.id ?: run {
                    val newProfessorId = UUID.randomUUID()
                    Professors.insert(
                        ProfessorDTO(
                            id = newProfessorId,
                            name = normalizedName,
                            department = normalizedDepartment,
                            position = normalizedPosition
                        )
                    )
                    newProfessorId
                }

                if (ArticleAuthors.exists(articleId, professorId)) {
                    duplicateAuthors.add(normalizedName)
                } else {
                    ArticleAuthors.insert(
                        ArticleAuthorDTO(
                            id = UUID.randomUUID(),
                            articleId = articleId,
                            professorId = professorId
                        )
                    )
                }
            }

            if (duplicateAuthors.isNotEmpty()) {
                call.respond(
                    HttpStatusCode.Conflict,
                    AddNewArticleResponseRemote(
                        message = "Дублирование: преподаватель(и) ${duplicateAuthors.joinToString(", ")} уже является(ются) автором данной статьи"
                    )
                )
            } else {
                call.respond(HttpStatusCode.OK, AddNewArticleResponseRemote(message = "Статья успешно добавлена"))
            }
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, "Ошибка при добавлении статьи: ${e.localizedMessage}")
        }
    }

    suspend fun getAllArticles() {
        val receive = call.receive<AuthorRemote>()

        val professorDTO = Professors.fetch(receive.name, receive.position, receive.department)
        if (professorDTO == null) {
            call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            return
        }

        val articleIds = ArticleAuthors.fetchByProfessor(professorDTO.id)
            .map { it.articleId }
            .toSet()

        if (articleIds.isEmpty()) {
            call.respond(HttpStatusCode.OK, GetAllArticlesResponseRemote(articles = emptyList()))
            return
        }

        val articles = articleIds.mapNotNull { articleId ->
            Articles.fetchById(articleId)
        }

        val responseList = articles.map { article ->
            ArticleRemote(
                title = article.title,
                link = article.link
            )
        }

        call.respond(HttpStatusCode.OK, GetAllArticlesResponseRemote(articles = responseList))
    }
}