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
            // 1. Вставляем статью в таблицу articles
            val articleId = UUID.randomUUID()
            Articles.insert(
                ArticleDTO(
                    id = articleId,
                    title = receive.title,
                    link = receive.link
                )
            )

            // 2. Для каждого автора из списка
            receive.authors.forEach { author ->
                // Ищем преподавателя по ФИО, кафедре и должности
                val professorDTO = Professors.fetch(author.name, author.department, author.position)
                // Если не найден – создаём нового
                val professorId = professorDTO?.id ?: run {
                    val newProfessorId = UUID.randomUUID()
                    Professors.insert(
                        ProfessorDTO(
                            id = newProfessorId,
                            name = author.name,
                            department = author.department,
                            position = author.position
                        )
                    )
                    newProfessorId
                }
                // 3. Привязываем преподавателя к статье
                ArticleAuthors.insert(
                    ArticleAuthorDTO(
                        id = UUID.randomUUID(),
                        articleId = articleId,
                        professorId = professorId
                    )
                )
            }
            call.respond(HttpStatusCode.OK, AddNewArticleResponseRemote(message = "Статья успешно добавлена"))
        } catch (e: ExposedSQLException) {
            call.respond(HttpStatusCode.Conflict, "Ошибка при добавлении статьи: ${e.localizedMessage}")
        }
    }

    suspend fun getAllArticles() {
        // Получаем данные из запроса
        val receive = call.receive<AuthorRemote>()

        // Ищем преподавателя
        val professorDTO = Professors.fetch(receive.name, receive.position, receive.department)
        if (professorDTO == null) {
            call.respond(HttpStatusCode.NotFound, "Преподаватель не найден")
            return
        }

        // Ищем статьи, в которых он автор
        val articleIds = ArticleAuthors.fetchByProfessor(professorDTO.id)
            .map { it.articleId }
            .toSet()

        if (articleIds.isEmpty()) {
            call.respond(HttpStatusCode.OK, GetAllArticlesResponseRemote(articles = emptyList()))
            return
        }

        // Получаем статьи по ID
        val articles = articleIds.mapNotNull { articleId ->
            Articles.fetchById(articleId)
        }

        // Формируем ответ
        val responseList = articles.map { article ->
            ArticleRemote(
                title = article.title,
                link = article.link
            )
        }

        call.respond(HttpStatusCode.OK, GetAllArticlesResponseRemote(articles = responseList))
    }
}