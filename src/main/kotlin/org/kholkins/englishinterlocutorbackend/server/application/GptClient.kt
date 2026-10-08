package org.kholkins.englishinterlocutorbackend.server.application

import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptRequest
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptResponse

interface GptClient {
    fun call(request: GptRequest): GptResponse
}