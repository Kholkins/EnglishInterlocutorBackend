package org.kholkins.englishinterlocutorbackend.server.infrastructure.dto

class CompletionOptions(
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    val maxTokens: Int? = null
)
