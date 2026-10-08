package org.kholkins.englishinterlocutorbackend.server.application.usecases

import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptMessage

interface GptUseCase {
    fun execute(messages: List<GptMessage>, model: String?): String
}