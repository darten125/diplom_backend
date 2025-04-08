package com.example.features.ProcessedRequestsFeatures

import kotlinx.serialization.Serializable

@Serializable
data class GetProcessedRequestsForUserRequest(
    val studentId: String
)

@Serializable
data class ProcessedRequestResponse(
    val id: String,
    val professorName: String,
    val professorPosition: String,
    val professorDepartment: String,
    val thesisTitle: String,
    val description: String,
    val accepted: Boolean
)

@Serializable
data class GetProcessedRequestsForUserResponse(
    val requests: List<ProcessedRequestResponse>
)