package org.kholkins.englishinterlocutorbackend.server.application

import org.kholkins.englishinterlocutorbackend.server.application.usecases.GptUseCase
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptMessage
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptRequest
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class GptService(
    private val gptClient: GptClient,
    @Value("\${yandex.folder-id}") private val folderId: String
) : GptUseCase {
    override fun execute(messages: List<GptMessage>, model: String?): String {

        val modelUri = "gpt://$folderId/${model ?: "yandexgpt-lite"}/latest"

        val request = GptRequest(
            modelUri = modelUri,
            messages = messages
        )
        val response: GptResponse = gptClient.call(request)

        return response.alternatives?.firstOrNull()?.message?.text
            ?: throw IllegalStateException("YandexGPT вернул пустой список alternatives")
    }
}