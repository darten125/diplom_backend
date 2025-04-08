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
            // Нормализуем входные данные для ссылки и названия (например, обрезаем пробелы)
            val normalizedLink = receive.link.trim()
            val normalizedTitle = receive.title.trim()

            // 1. Проверяем, существует ли уже статья с таким же link
            val existingArticle = Articles.fetchByLink(normalizedLink)
            val articleId = if (existingArticle != null) {
                existingArticle.id
            } else {
                // Если не существует, создаем новую статью
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

            // 2. Для каждого автора из списка
            // Собираем список дублирующихся преподавателей
            val duplicateAuthors = mutableListOf<String>()

            receive.authors.forEach { author ->
                // Нормализуем данные автора
                val normalizedName = author.name.trim()
                val normalizedPosition = author.position.trim()
                val normalizedDepartment = author.department.trim()

                // Вызов метода fetch с правильным порядком параметров: (name, position, department)
                val professorDTO = Professors.fetch(normalizedName, normalizedPosition, normalizedDepartment)
                // Если не найден – создаём нового
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

                // 3. Проверяем, является ли преподаватель уже автором этой статьи
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