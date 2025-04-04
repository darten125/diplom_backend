package com.example.features.getPreviousTheses

import kotlinx.serialization.Serializable

@Serializable
data class GetPreviousThesesRequestRemote(
    val name: String,
    val position: String,
    val department: String
)

@Serializable
data class PreviousThesisRemote(
    val title: String
)

@Serializable
data class GetPreviousThesesResponseRemote(
    val theses: List<PreviousThesisRemote>
)