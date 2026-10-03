package org.kholkins.englishinterlocutorbackend.server.application.usecases

import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateRequest
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateResponse

interface TranslateUseCase {
    fun translate(request: TranslateRequest): TranslateResponse
}