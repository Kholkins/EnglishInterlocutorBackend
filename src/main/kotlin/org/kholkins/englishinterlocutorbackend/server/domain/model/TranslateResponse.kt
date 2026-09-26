package org.kholkins.englishinterlocutorbackend.server.domain.model

data class TranslateResponse(
    val translatedText: String,
    val sourceText: String,
    val detectedLanguage: String? = null
)
