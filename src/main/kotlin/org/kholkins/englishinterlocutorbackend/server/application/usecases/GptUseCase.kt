package org.kholkins.englishinterlocutorbackend.server.application.usecases

import org.kholkins.englishinterlocutorbackend.server.infrastructure.GptClient
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.CompletionOptions
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptRequest
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptMessage
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GptUseCase(
    private val gptClient: GptClient,
    @Value("\${yandex.folder-id}") private val folderId: String
) {
    fun execute(messages: List<GptMessage>, model: String?): String {

        val modelUri = "gpt://$folderId/${model ?: "yandexgpt-lite"}/latest"

        val request = GptRequest(
            modelUri = modelUri,
            messages = messages
        )
        try {
            val response: GptResponse = gptClient.call(request)

            return response.alternatives?.firstOrNull()?.message?.text
                ?: throw IllegalStateException("YandexGPT вернул пустой список alternatives")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}