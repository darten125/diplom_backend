package com.example.features.professorsFeatures.articlesFeatures

import kotlinx.serialization.Serializable

@Serializable
data class AddNewArticleReceiveRemote(
    val title: String,
    val link: String,
    val authors: List<AuthorRemote>
)

@Serializable
data class AuthorRemote(
    val name: String,
    val department: String,
    val position: String
)

@Serializable
data class AddNewArticleResponseRemote(
    val message: String
)

@Serializable
data class ArticleRemote(
    val title: String,
    val link: String
)

@Serializable
data class GetAllArticlesResponseRemote(
    val articles: List<ArticleRemote>
)