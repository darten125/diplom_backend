package com.example.features.tokensFeatures
import kotlinx.serialization.Serializable

@Serializable
data class TokenValidationReceiveRemote(
    val token: String
)

@Serializable
data class TokenValidationResponseRemote(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val currentThesisId: String?,
    val userGroup: String
)

@Serializable
data class DeleteTokenReceiveRemote(
    val id: String
)