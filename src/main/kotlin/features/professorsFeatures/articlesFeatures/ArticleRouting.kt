package com.example.features.professorsFeatures.articlesFeatures

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureArticleRouting() {
    routing {
        post("/add-new-article") {
            val controller = ArticleController(call)
            controller.addNewArticle()
        }

        post("/get-all-articles") {
            val controller = ArticleController(call)
            controller.getAllArticles()
        }
    }
}