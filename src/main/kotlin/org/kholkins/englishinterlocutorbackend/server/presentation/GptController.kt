package org.kholkins.englishinterlocutorbackend.server.presentation

import org.kholkins.englishinterlocutorbackend.server.application.usecases.GptUseCase
import org.kholkins.englishinterlocutorbackend.server.domain.model.ChatRequest
import org.kholkins.englishinterlocutorbackend.server.domain.model.ChatResponse
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptMessage
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/gpt")
class GptController(
    private val gptUseCase: GptUseCase
) {
    @PostMapping("/chat")
    fun chat(@RequestBody req: ChatRequest): ChatResponse {
        val gptMessages = req.messages.map { msg ->
            GptMessage(role = msg.role, text = msg.text)
        }

        val resultText = gptUseCase.execute(gptMessages, req.model)
        return ChatResponse(text = resultText)
    }
}
