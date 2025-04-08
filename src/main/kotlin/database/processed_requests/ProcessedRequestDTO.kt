package com.example.database.processed_requests

import java.util.*

class ProcessedRequestDTO(
    val id: UUID,
    val studentId: UUID,
    val professorId: UUID,
    val thesisTitle: String,
    val description: String,
    val accepted: Boolean
)

class CurrentThesisDTO (
    val id: UUID,
    val studentId: UUID,
    val professorId: UUID,
    val title: String,
    val description: String
)