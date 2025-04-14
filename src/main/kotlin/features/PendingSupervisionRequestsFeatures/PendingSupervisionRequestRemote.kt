package com.example.features.PendingSupervisionRequestsFeatures

import kotlinx.serialization.Serializable

@Serializable
data class CreatePendingRequestRemote(
    val studentId: String,
    val professorId: String,
    val thesisTitle: String,
    val description: String
)

@Serializable
data class GetPendingRequestsForUserRequest(
    val studentId: String
)

@Serializable
data class PendingRequestResponse(
    val id: String,
    val professorName: String,
    val professorPosition: String,
    val professorDepartment: String,
    val thesisTitle: String,
    val description: String,
    val accepted: Boolean?
)

@Serializable
data class GetPendingRequestsForUserResponse(
    val requests: List<PendingRequestResponse>
)