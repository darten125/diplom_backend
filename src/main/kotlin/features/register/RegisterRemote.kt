package com.example.features.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterReceiveRemote(
    val name: String,
    val role: String,
    val email: String,
    val password: String,
    val userGroup: String
)

@Serializable
data class RegisterResponseRemote(
    val token: String
)
