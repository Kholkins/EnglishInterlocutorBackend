package org.kholkins.englishinterlocutorbackend.server.presentation

import org.kholkins.englishinterlocutorbackend.server.application.TranslateUseCase
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateRequest
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class TranslationController(
    private val translateUseCase: TranslateUseCase
) {
    @PostMapping("/translate")
    fun translate(@RequestBody request: TranslateRequest): TranslateResponse {
        return translateUseCase.translate(request)
    }

    @GetMapping("/ping")
    fun ping(): String = "ok"
}