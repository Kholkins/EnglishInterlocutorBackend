package org.kholkins.englishinterlocutorbackend.server.infrastructure

import org.kholkins.englishinterlocutorbackend.server.application.TranslateClient
import org.kholkins.englishinterlocutorbackend.server.application.TranslateResult
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.YandexTranslateRequest
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.YandexTranslateResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class YandexTranslateClient(
    @Value("\${yandex.api-key}") private val apiKey: String,
    @Value("\${yandex.folder-id}") private val folderId: String
) : TranslateClient {

    private val log = LoggerFactory.getLogger(javaClass)

    private val client = RestClient.builder()
        .baseUrl("https://translate.api.cloud.yandex.net/translate/v2")
        .defaultHeader("Authorization", "Api-Key $apiKey")
        .build()

    override fun translate(text: String, targetLang: String): TranslateResult {
        log.info("Translating '{}' -> {}", text, targetLang)

        val request = YandexTranslateRequest(
            folderId = folderId,
            texts = listOf(text),
            targetLanguageCode = targetLang
        )

        val entity = client.post()
            .uri("/translate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toEntity(YandexTranslateResponse::class.java)

        val response = entity.body
            ?: throw IllegalStateException("Yandex returned null body")

        val translation = response.translations.firstOrNull()
            ?: throw IllegalStateException("Yandex returned no translations")

        log.info(
            "Translated: '{}' -> '{}' (detected: {})",
            text, translation.text, translation.detectedLanguageCode
        )

        return TranslateResult(
            translatedText = translation.text,
            detectedLanguage = translation.detectedLanguageCode
        )
    }
}


