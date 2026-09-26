package org.kholkins.englishinterlocutorbackend.server.application

import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateRequest
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateResponse
import org.springframework.stereotype.Service

@Service
class TranslateService(
    private val translateClient: TranslateClient
) : TranslateUseCase {

    override fun translate(request: TranslateRequest): TranslateResponse {
        val result = translateClient.translate(
            text = request.text,
            targetLang = request.targetLang
        )

        return TranslateResponse(
            translatedText = result.translatedText,
            sourceText = request.text,
            detectedLanguage = result.detectedLanguage
        )
    }
}