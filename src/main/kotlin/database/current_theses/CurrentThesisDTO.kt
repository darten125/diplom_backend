package com.example.database.current_theses

import java.util.*

class CurrentThesisDTO (
    val id: UUID,
    val studentId: UUID,
    val professorId: UUID,
    val title: String,
    val description: String
)