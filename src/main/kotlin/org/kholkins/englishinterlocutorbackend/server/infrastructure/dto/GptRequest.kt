package org.kholkins.englishinterlocutorbackend.server.infrastructure.dto

data class GptRequest(
    val messages: List<GptMessage>,
    val modelUri: String,
    val completionOptions: CompletionOptions = CompletionOptions()
)
