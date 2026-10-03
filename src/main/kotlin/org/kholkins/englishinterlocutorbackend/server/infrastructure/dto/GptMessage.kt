package org.kholkins.englishinterlocutorbackend.server.infrastructure.dto

data class GptMessage(
    val role: String,
    val text: String
)
