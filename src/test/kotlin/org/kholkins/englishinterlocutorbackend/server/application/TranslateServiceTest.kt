package org.kholkins.englishinterlocutorbackend.server.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kholkins.englishinterlocutorbackend.server.domain.model.TranslateRequest
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class TranslateServiceTest {

    private lateinit var translateClient: TranslateClient
    private lateinit var translateService: TranslateService

    @BeforeEach
    fun setUp() {
        translateClient = mock()
        translateService = TranslateService(translateClient)
    }

    // ---------- happy path ----------

    @Test
    fun `should return response with translated text and source text`() {
        whenever(translateClient.translate("Hello", "ru"))
            .thenReturn(TranslateResult("Привет", "en"))

        val response = translateService.translate(
            TranslateRequest(text = "Hello", targetLang = "ru")
        )

        assertThat(response.translatedText).isEqualTo("Привет")
        assertThat(response.sourceText).isEqualTo("Hello")
        assertThat(response.detectedLanguage).isEqualTo("en")
    }

    @Test
    fun `should pass exact text and target lang to client`() {
        whenever(translateClient.translate(any(), any()))
            .thenReturn(TranslateResult("ok", "en"))

        translateService.translate(
            TranslateRequest(text = "Good morning", targetLang = "de")
        )

        verify(translateClient).translate("Good morning", "de")
    }

    @Test
    fun `should not forward sourceLang to client`() {
        whenever(translateClient.translate(any(), any()))
            .thenReturn(TranslateResult("ok", "en"))

        translateService.translate(
            TranslateRequest(text = "Hello", sourceLang = "fr", targetLang = "ru")
        )

        verify(translateClient).translate("Hello", "ru")
    }

    // ---------- nullable / edge cases ----------

    @Test
    fun `should keep null detected language from client`() {
        whenever(translateClient.translate(any(), any()))
            .thenReturn(TranslateResult("Привет", detectedLanguage = null))

        val response = translateService.translate(TranslateRequest(text = "Hello"))

        assertThat(response.translatedText).isEqualTo("Привет")
        assertThat(response.detectedLanguage).isNull()
    }

    @Test
    fun `should pass empty string through without validation`() {
        whenever(translateClient.translate("", "ru"))
            .thenReturn(TranslateResult("", null))

        val response = translateService.translate(
            TranslateRequest(text = "", targetLang = "ru")
        )

        assertThat(response.sourceText).isEmpty()
        verify(translateClient).translate("", "ru")
    }

    @Test
    fun `should use default target lang from request`() {
        whenever(translateClient.translate(any(), any()))
            .thenReturn(TranslateResult("ok", "en"))

        translateService.translate(TranslateRequest(text = "Hello"))

        verify(translateClient).translate("Hello", "ru")
    }

    // ---------- error handling ----------

    @Test
    fun `should propagate exception from client`() {
        whenever(translateClient.translate(any(), any()))
            .thenThrow(RuntimeException("Yandex Translate error: 500"))

        val ex = assertThrows<RuntimeException> {
            translateService.translate(TranslateRequest(text = "Hello"))
        }

        assertThat(ex.message).contains("Yandex Translate error")
    }

    @Test
    fun `should not call client when building response fails early`() {
        whenever(translateClient.translate(any(), any()))
            .thenReturn(TranslateResult("ok", "en"))

        translateService.translate(TranslateRequest(text = "Hello"))

        verify(translateClient).translate(any(), any())
    }
}