package org.kholkins.englishinterlocutorbackend.server.infrastructure

import org.kholkins.englishinterlocutorbackend.server.application.GptClient
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptRequest
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptResponse
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class YandexGptClient(
    @Value("\${YANDEX_API_KEY}") private val apiKey: String,
    @Value("\${yandex.gpt.url}") private val url: String,
    private val objectMapper: ObjectMapper
) : GptClient {
    private val client = HttpClient.newHttpClient()

    override fun call(request: GptRequest): GptResponse {
        val body = objectMapper.writeValueAsString(request)
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Api-Key $apiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        if (httpResponse.statusCode() != 200) {
            throw IllegalStateException(
                "YandexGPT error: ${httpResponse.statusCode()} ${httpResponse.body()}"
            )
        }

        val rootNode = objectMapper.readTree(httpResponse.body())
        val resultNode = rootNode.get("result")
            ?: throw IllegalStateException("В ответе нет поля 'result': ${httpResponse.body()}")

        return objectMapper.treeToValue(resultNode, GptResponse::class.java)
    }

}