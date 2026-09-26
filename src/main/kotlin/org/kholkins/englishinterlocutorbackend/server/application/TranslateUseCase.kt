package org.kholkins.englishinterlocutorbackend.server.application

import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateRequest
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateResponse

interface TranslateUseCase {
    fun translate(request: TranslateRequest): TranslateResponse
}