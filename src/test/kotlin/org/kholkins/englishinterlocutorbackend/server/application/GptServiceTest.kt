package org.kholkins.englishinterlocutorbackend.server.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.kholkins.englishinterlocutorbackend.server.application.usecases.GptUseCase
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.Alternative
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptMessage
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptRequest
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptResponse
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class GptServiceTest {

    private lateinit var gptUseCase: GptUseCase

    private lateinit var gptClient: GptClient

    private val folderId = "test-folder-id"

    @BeforeEach
    fun setUp() {
        gptClient = mock()
        gptUseCase = GptService(gptClient, folderId)
    }

    // ---------- happy path ----------

    @Test
    fun `should return text from first alternative`() {
        whenever(gptClient.call(any())).thenReturn(gptResponseWith(text = "Hi there!"))

        val result = gptUseCase.execute(
            messages = listOf(GptMessage(role = "user", text = "Hello")),
            model = null
        )

        assertThat(result).isEqualTo("Hi there!")
        verify(gptClient).call(any())
    }

    // ---------- modelUri ----------

    @Test
    fun `should use yandexgpt-lite when model is null`() {
        whenever(gptClient.call(any())).thenReturn(gptResponseWith("ok"))

        gptUseCase.execute(
            messages = listOf(GptMessage("user", "Hello")),
            model = null
        )

        val captor = argumentCaptor<GptRequest>()
        verify(gptClient).call(captor.capture())
        assertThat(captor.firstValue.modelUri)
            .isEqualTo("gpt://$folderId/yandexgpt-lite/latest")
    }

    @Test
    fun `should use provided model when specified`() {
        whenever(gptClient.call(any())).thenReturn(gptResponseWith("ok"))

        gptUseCase.execute(
            messages = listOf(GptMessage("user", "Hello")),
            model = "yandexgpt-pro"
        )

        val captor = argumentCaptor<GptRequest>()
        verify(gptClient).call(captor.capture())
        assertThat(captor.firstValue.modelUri)
            .isEqualTo("gpt://$folderId/yandexgpt-pro/latest")
    }

    @Test
    fun `should pass messages unchanged to client`() {
        whenever(gptClient.call(any())).thenReturn(gptResponseWith("ok"))
        val messages = listOf(
            GptMessage("system", "You are a tutor"),
            GptMessage("user", "Hello")
        )

        gptUseCase.execute(messages, model = null)

        val captor = argumentCaptor<GptRequest>()
        verify(gptClient).call(captor.capture())
        assertThat(captor.firstValue.messages).isEqualTo(messages)
    }

    // ---------- error handling ----------

    @Test
    fun `should throw when alternatives is null`() {
        whenever(gptClient.call(any()))
            .thenReturn(GptResponse(alternatives = null))

        val ex = assertThrows<IllegalStateException> {
            gptUseCase.execute(listOf(GptMessage("user", "Hi")), model = null)
        }
        assertThat(ex.message).contains("alternatives")
    }

    @Test
    fun `should throw when alternatives is empty`() {
        whenever(gptClient.call(any()))
            .thenReturn(GptResponse(alternatives = emptyList()))

        val ex = assertThrows<IllegalStateException> {
            gptUseCase.execute(listOf(GptMessage("user", "Hi")), model = null)
        }
        assertThat(ex.message).contains("alternatives")
    }

    @Test
    fun `should propagate exception from client`() {
        whenever(gptClient.call(any()))
            .thenThrow(RuntimeException("YandexGPT error: 500"))

        val ex = assertThrows<RuntimeException> {
            gptUseCase.execute(listOf(GptMessage("user", "Hi")), model = null)
        }
        assertThat(ex.message).contains("YandexGPT error")
    }

    // ---------- helper ----------

    private fun gptResponseWith(text: String): GptResponse =
        GptResponse(
            alternatives = listOf(
                Alternative(message = GptMessage(role = "assistant", text = text))
            ),
            modelVersion = "test-version"
        )
}