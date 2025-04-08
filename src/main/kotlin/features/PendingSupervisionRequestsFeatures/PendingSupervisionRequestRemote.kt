package com.example.features.PendingSupervisionRequestsFeatures

import kotlinx.serialization.Serializable

@Serializable
data class CreatePendingRequestRemote(
    val studentId: String,
    val professorId: String,
    val thesisTitle: String,
    val description: String
)