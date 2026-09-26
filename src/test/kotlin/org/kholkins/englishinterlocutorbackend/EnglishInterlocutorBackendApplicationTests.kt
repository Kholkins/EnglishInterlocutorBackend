package org.kholkins.englishinterlocutorbackend

import org.junit.jupiter.api.Test
import org.kholkins.englishinterlocutorbackend.server.application.TranslateClient
import org.kholkins.englishinterlocutorbackend.server.application.TranslateResult
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootTest(properties = ["spring.profiles.active=test"])
class EnglishInterlocutorBackendApplicationTests {

    @Test
    fun contextLoads() {
        // Пустой тест: если контекст поднялся без ошибок — тест пройден
    }

    // Эта конфигурация работает ТОЛЬКО в тестах и заменяет реальный YandexTranslateClient на мок
    @Configuration
    class TestConfig {
        @Bean
        fun mockTranslateClient(): TranslateClient = object : TranslateClient {
            override fun translate(text: String, targetLang: String): TranslateResult {
                // Возвращаем предсказуемый результат для тестов
                return TranslateResult(
                    translatedText = "mocked: $text",
                    detectedLanguage = "en"
                )
            }
        }
    }
}
