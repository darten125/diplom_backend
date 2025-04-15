package com.example.features.professorsFeatures

import kotlinx.serialization.Serializable

@Serializable
data class GetAllProfessorsResponse(
    val professors: List<ProfessorsRemote>
)

@Serializable
data class ProfessorsRemote(
    val id: String,
    val name: String,
    val position: String,
    val department: String
)

@Serializable
data class UpdateProfessorReceiveRemote(
    val name: String,
    val oldPosition: String,
    val oldDepartment: String,
    val newPosition: String,
    val newDepartment: String
)

@Serializable
data class UpdateProfessorResponseRemote(
    val message: String
)

@Serializable
data class DeleteProfessorReceiveRemote(
    val name: String,
    val position: String,
    val department: String
)