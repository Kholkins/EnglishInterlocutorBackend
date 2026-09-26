package org.kholkins.englishinterlocutorbackend.server.infrastructure.dto

data class YandexTranslateResponse(
    val translations: List<YandexTranslation>
)

data class YandexTranslation(
    val text: String,
    val detectedLanguageCode: String? = null
)
