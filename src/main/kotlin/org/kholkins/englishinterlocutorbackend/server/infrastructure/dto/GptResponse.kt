package org.kholkins.englishinterlocutorbackend.server.infrastructure.dto

data class GptResponse(
    val alternatives: List<Alternative>? = null,
    val modelVersion: String? = null
)

data class Alternative(
    val message: GptMessage,
    val status: String? = null
)
