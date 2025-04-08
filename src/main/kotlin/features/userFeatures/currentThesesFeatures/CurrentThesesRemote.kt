package com.example.features.userFeatures.currentThesesFeatures

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateCurrentThesisReceiveRemote(
    val processedRequestId: String
)

@Serializable
data class CreateCurrentThesisResponseRemote(
    val message: String
)

@Serializable
data class GetCurrentThesisReceiveRemote(
    val studentId: String
)

@Serializable
data class GetCurrentThesisResponseRemote(
    val thesis: CurrentThesisResponseRemote?
)

@Serializable
data class CurrentThesisResponseRemote(
    val professorName: String,
    val professorPosition: String,
    val professorDepartment: String,
    val title: String,
    val description: String
)
