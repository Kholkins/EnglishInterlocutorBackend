package org.kholkins.englishinterlocutorbackend.server.presentation

import org.junit.jupiter.api.Test
import org.kholkins.englishinterlocutorbackend.server.application.usecases.GptUseCase
import org.kholkins.englishinterlocutorbackend.server.infrastructure.dto.GptMessage
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GptController::class)
class GptControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var gptUseCase: GptUseCase

    // ---------- happy path ----------

    @Test
    fun `should return 200 with gpt response`() {
        whenever(gptUseCase.execute(any(), anyOrNull()))
            .thenReturn("Hi there!")

        mockMvc.perform(
            post("/api/gpt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "messages": [
                        {"role": "user", "text": "Hello"}
                      ]
                    }
                    """.trimIndent()
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.text").value("Hi there!"))
    }

    // ---------- mapping ----------

    @Test
    fun `should map request messages to gpt messages`() {
        whenever(gptUseCase.execute(any(), anyOrNull())).thenReturn("ok")

        mockMvc.perform(
            post("/api/gpt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "messages": [
                        {"role": "system", "text": "You are a tutor"},
                        {"role": "user",   "text": "Hello"}
                      ]
                    }
                    """.trimIndent()
                )
        ).andExpect(status().isOk)

        val captor = argumentCaptor<List<GptMessage>>()
        verify(gptUseCase).execute(captor.capture(), anyOrNull())

        val messages = captor.firstValue
        assert(messages.size == 2)
        assert(messages[0] == GptMessage("system", "You are a tutor"))
        assert(messages[1] == GptMessage("user", "Hello"))
    }

    @Test
    fun `should pass null model when not provided`() {
        whenever(gptUseCase.execute(any(), anyOrNull())).thenReturn("ok")

        mockMvc.perform(
            post("/api/gpt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"messages": [{"role": "user", "text": "Hi"}]}
                    """.trimIndent()
                )
        ).andExpect(status().isOk)

        verify(gptUseCase).execute(any(), isNull())
    }

    @Test
    fun `should pass provided model to use case`() {
        whenever(gptUseCase.execute(any(), anyOrNull())).thenReturn("ok")

        mockMvc.perform(
            post("/api/gpt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "messages": [{"role": "user", "text": "Hi"}],
                      "model": "yandexgpt-pro"
                    }
                    """.trimIndent()
                )
        ).andExpect(status().isOk)

        verify(gptUseCase).execute(any(), eq("yandexgpt-pro"))
    }

    // ---------- validation / errors ----------

    @Test
    fun `should return 400 on malformed json`() {
        mockMvc.perform(
            post("/api/gpt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"messages": }""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 on empty body`() {
        mockMvc.perform(
            post("/api/gpt/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("")
        )
            .andExpect(status().isBadRequest)
    }
}