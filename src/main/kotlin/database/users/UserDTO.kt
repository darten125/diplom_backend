package com.example.database.users

import java.util.*

class UserDTO (
    val id: UUID,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val currentThesisId: UUID?,
    val userGroup: String
)