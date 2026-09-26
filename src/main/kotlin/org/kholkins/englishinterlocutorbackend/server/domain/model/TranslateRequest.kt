package org.kholkins.englishinterlocutorbackend.server.domain.model

data class TranslateRequest(
    val text: String,
    val sourceLang: String = "en",
    val targetLang: String = "ru"
)
