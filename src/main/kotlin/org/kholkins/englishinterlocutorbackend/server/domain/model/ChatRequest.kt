package org.kholkins.englishinterlocutorbackend.server.domain.model

data class ChatRequest(
    val messages: List<Message>,
    val model: String? = null
)

data class Message(
    val role: String,
    val text: String
)