package org.kholkins.englishinterlocutorbackend

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.kholkins.englishinterlocutorbackend.server.application.TranslateClient
import org.kholkins.englishinterlocutorbackend.server.application.TranslateResult
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "YANDEX_API_KEY=test-key",
    "YANDEX_FOLDER_ID=test-folder"
])
class EnglishInterlocutorBackendApplicationTests {

    @MockitoBean
    private lateinit var translateClient: TranslateClient

    @Test
    fun `context loads with mocked translate client`() {
        whenever(translateClient.translate(any(), any()))
            .thenReturn(TranslateResult("mocked", "en"))

        val result = translateClient.translate("Hello", "ru")

        assertThat(result.translatedText).isEqualTo("mocked")
        assertThat(result.detectedLanguage).isEqualTo("en")
    }
}
