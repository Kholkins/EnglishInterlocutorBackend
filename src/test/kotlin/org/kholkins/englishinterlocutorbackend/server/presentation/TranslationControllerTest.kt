package org.kholkins.englishinterlocutorbackend.server.presentation

import org.junit.jupiter.api.Test
import org.kholkins.englishinterlocutorbackend.server.application.usecases.TranslateUseCase
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateRequest
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateResponse
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TranslationController::class)
class TranslationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var translateUseCase: TranslateUseCase

    // ---------- POST /api/translate ----------

    @Test
    fun `should return 200 with translation`() {
        whenever(translateUseCase.translate(any())).thenReturn(
            TranslateResponse(
                translatedText = "Привет",
                sourceText = "Hello",
                detectedLanguage = "en"
            )
        )

        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"Hello","targetLang":"ru"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.translatedText").value("Привет"))
            .andExpect(jsonPath("$.sourceText").value("Hello"))
            .andExpect(jsonPath("$.detectedLanguage").value("en"))
    }

    @Test
    fun `should pass request body as-is to use case`() {
        whenever(translateUseCase.translate(any())).thenReturn(
            TranslateResponse("Привет", "Hello", "en")
        )

        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "text": "Hello",
                      "sourceLang": "fr",
                      "targetLang": "ru"
                    }
                    """.trimIndent()
                )
        ).andExpect(status().isOk)

        val captor = argumentCaptor<TranslateRequest>()
        verify(translateUseCase).translate(captor.capture())

        val request = captor.firstValue
        assert(request.text == "Hello")
        assert(request.sourceLang == "fr")
        assert(request.targetLang == "ru")
    }

    @Test
    fun `should use default source and target langs when not provided`() {
        whenever(translateUseCase.translate(any())).thenReturn(
            TranslateResponse("Привет", "Hello", "en")
        )

        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"Hello"}""")
        ).andExpect(status().isOk)

        val captor = argumentCaptor<TranslateRequest>()
        verify(translateUseCase).translate(captor.capture())

        val request = captor.firstValue
        assert(request.sourceLang == "en")
        assert(request.targetLang == "ru")
    }

    @Test
    fun `should keep null detected language in response`() {
        whenever(translateUseCase.translate(any())).thenReturn(
            TranslateResponse(
                translatedText = "Привет",
                sourceText = "Hello",
                detectedLanguage = null
            )
        )

        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":"Hello"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.translatedText").value("Привет"))
            // jsonPath по умолчанию не отличает null от отсутствия поля,
            // поэтому проверяем отсутствие значения явно
            .andExpect(jsonPath("$.detectedLanguage").value(null as String?))
    }

    // ---------- ошибки тела запроса ----------

    @Test
    fun `should return 400 on malformed json`() {
        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"text":}""")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(translateUseCase)
    }

    @Test
    fun `should return 400 on empty body`() {
        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(translateUseCase)
    }

    @Test
    fun `should return 400 on missing text field`() {
        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"targetLang":"ru"}""")
        )
            .andExpect(status().isBadRequest)

        verifyNoInteractions(translateUseCase)
    }

    @Test
    fun `should return 415 on unsupported content type`() {
        mockMvc.perform(
            post("/api/translate")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Hello")
        )
            .andExpect(status().isUnsupportedMediaType)

        verifyNoInteractions(translateUseCase)
    }

    // ---------- GET /api/ping ----------

    @Test
    fun `ping should return ok`() {
        mockMvc.perform(get("/api/ping"))
            .andExpect(status().isOk)
            .andExpect(content().string("ok"))

        verifyNoInteractions(translateUseCase)
    }
}