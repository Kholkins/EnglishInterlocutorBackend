package org.kholkins.englishinterlocutorbackend.server.infrastructure.dto

data class YandexTranslateRequest(
    val folderId: String,
    val texts: List<String>,
    val targetLanguageCode: String
)
