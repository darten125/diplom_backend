package com.example.features.userFeatures

import kotlinx.serialization.Serializable

@Serializable
data class GetUserDataReceiveRemote(
    val email: String
)

@Serializable
data class GetUserDataResponseRemote(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val currentThesisId: String?, // может быть null
    val userGroup: String
)