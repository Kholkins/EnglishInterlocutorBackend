package org.kholkins.englishinterlocutorbackend.server.application

interface TranslateClient {
    fun translate(text: String, targetLang: String): TranslateResult
}

data class TranslateResult(
    val translatedText: String,
    val detectedLanguage: String?
)