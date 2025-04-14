package com.example.database.pending_supervision_requests

import java.util.*

class PendingSupervisionRequestDTO(
    val id: UUID,
    val studentId: UUID,
    val professorId: UUID,
    val thesisTitle: String,
    val description: String,
    val accepted: Boolean?
)