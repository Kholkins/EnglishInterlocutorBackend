# EnglishInterlocutorBackend

Бэкенд для приложения-собеседника для практики английского языка.
Предоставляет REST API для диалога с LLM (YandexGPT) и перевода текста (Yandex Translate).

## Стек

- **Язык:** Kotlin 2.x
- **Платформа:** Java 21
- **Фреймворк:** Spring Boot 4.1.1
- **Сборка:** Gradle 8.14.5 (Kotlin DSL)
- **Сериализация:** Jackson 3 (`tools.jackson`)
- **HTTP-клиент:** `java.net.http.HttpClient` (JDK)
- **Тесты:** JUnit 5, Mockito-Kotlin, MockMvc
- **CI/CD:** Jenkins (Jenkinsfile)
- **Контейнеризация:** Docker

## Архитектура

Проект построен по принципам **Clean Architecture** с инверсией зависимостей:

```
presentation  →  usecases (интерфейсы)
                      ↑
                application (реализации)
                      ↓
                clients (интерфейсы)
                      ↑
                infrastructure (реализации)
```

Зависимости идут **только в сторону абстракций**:

- `presentation` знает только про интерфейсы use cases.
- `application` реализует use cases и зависит от интерфейсов клиентов.
- `infrastructure` реализует клиенты (YandexGPT, Yandex Translate).
- `domain` не зависит ни от чего.

Такая схема позволяет:

- легко заменять внешние интеграции (YandexGPT → OpenAI, Yandex Translate → DeepL);
- мокать зависимости в тестах без поднятия Spring-контекста;
- не тянуть инфраструктуру в бизнес-логику.

### Структура пакетов

```
server/
├── application/                # Бизнес-логика и порты
│   ├── GptClient.kt            # интерфейс
│   ├── GptService.kt           # реализация GptUseCase
│   ├── TranslateClient.kt      # интерфейс
│   ├── TranslateService.kt     # реализация TranslateUseCase
│   └── usecases/
│       ├── GptUseCase.kt       # интерфейс
│       └── TranslateUseCase.kt # интерфейс
├── domain/
│   └── model/                  # DTO домена
│       ├── ChatRequest.kt
│       ├── ChatResponse.kt
│       ├── TranslateRequest.kt
│       └── TranslateResponse.kt
├── infrastructure/             # Внешние интеграции
│   ├── YandexGptClient.kt
│   ├── YandexTranslateClient.kt
│   └── dto/
│       ├── Alternative.kt
│       ├── GptMessage.kt
│       ├── GptRequest.kt
│       └── GptResponse.kt
└── presentation/               # REST-контроллеры
    ├── GptController.kt
    └── TranslationController.kt
```

## API

### `POST /api/gpt/chat`

Отправляет список сообщений в YandexGPT и возвращает ответ.

**Запрос:**

```json
{
  "messages": [
    { "role": "system", "text": "You are a helpful English tutor." },
    { "role": "user",   "text": "Explain the difference between 'make' and 'do'." }
  ],
  "model": "yandexgpt-pro"
}
```

`model` опционален. Если не указан — используется `yandexgpt-lite`.

**Ответ:**

```json
{
  "text": "'Make' is used for creating or producing something, while 'do' is used for actions or tasks..."
}
```

### `POST /api/translate`

Переводит текст через Yandex Translate.

**Запрос:**

```json
{
  "text": "Hello, world!",
  "sourceLang": "en",
  "targetLang": "ru"
}
```

`sourceLang` (`en`) и `targetLang` (`ru`) опциональны.

**Ответ:**

```json
{
  "translatedText": "Привет, мир!",
  "sourceText": "Hello, world!",
  "detectedLanguage": "en"
}
```

### `GET /api/ping`

Health-check.

**Ответ:**

```
ok
```

## Запуск

### Локально

1. **Установите переменные окружения:**

   ```bash
   export YANDEX_API_KEY="your-yandex-api-key"
   ```

2. **Создайте `application-local.yml`** (или используйте переменные окружения):

   ```yaml
   yandex:
     folder-id: "your-folder-id"
     gpt:
       url: "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"
   ```

3. **Запустите приложение:**

   ```bash
   ./gradlew bootRun
   ```

   Приложение поднимется на `http://localhost:8080`.

### Через Docker

```bash
docker build -t english-interlocutor-backend .
docker run -p 8080:8080 \
  -e YANDEX_API_KEY="your-api-key" \
  -e YANDEX_FOLDER_ID="your-folder-id" \
  english-interlocutor-backend
```

### Проверка

```bash
curl http://localhost:8080/api/ping
# ok
```

## Переменные окружения

| Переменная            | Обязательна | Описание                       |
|-----------------------|-------------|--------------------------------|
| `YANDEX_API_KEY`      | да          | API-ключ Yandex Cloud          |
| `yandex.folder-id`    | да          | ID каталога в Yandex Cloud     |
| `yandex.gpt.url`      | да          | URL эндпоинта YandexGPT        |

## Тесты

Проект покрыт тестами на трёх уровнях:

| Уровень   | Класс теста                              | Что проверяет                                        |
|-----------|------------------------------------------|------------------------------------------------------|
| Smoke     | `EnglishInterlocutorBackendApplicationTests` | Поднятие Spring-контекста с моками внешних клиентов |
| Unit      | `GptServiceTest`                         | Логика `GptService` (сборка `modelUri`, обработка ошибок) |
| Unit      | `TranslateServiceTest`                   | Логика `TranslateService`                            |
| MVC       | `GptControllerTest`                      | HTTP-слой `/api/gpt/chat`                            |
| MVC       | `TranslationControllerTest`              | HTTP-слой `/api/translate`, `/api/ping`              |

**Запуск всех тестов:**

```bash
./gradlew test
```

**Запуск одного класса:**

```bash
./gradlew test --tests "*GptServiceTest"
```

Отчёт о прогоне: `build/reports/tests/test/index.html`.

### Особенности тестов

- **Внешние клиенты мокаются** через `@MockitoBean` (Spring Boot 4) — тесты не ходят в Yandex и не требуют API-ключей.
- **`@WebMvcTest`** для контроллеров — поднимается только веб-слой, без инфраструктуры.
- **Mockito-Kotlin** для идиоматичных моков (`whenever`, `anyOrNull`, `argumentCaptor`).

## Технические детали

### Jackson 3 и Kotlin

Проект использует **Jackson 3** (`tools.jackson.*`), который идёт в комплекте со Spring Boot 4.
Для корректной десериализации Kotlin-классов с default-значениями подключён модуль:

```gradle
implementation("tools.jackson.module:jackson-module-kotlin:3.1.0")
```

Без него клиент, не передавший опциональное поле (например, `targetLang`), получал бы `400 Bad Request`.

### Mock-фреймворк

В Spring Boot 4 аннотация `@MockBean` **удалена**. Вместо неё используется:

```kotlin
import org.springframework.test.context.bean.override.mockito.MockitoBean
```

### Пакет для `@WebMvcTest`

В Spring Boot 4 аннотация переехала:

```kotlin
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
```

и требует явной зависимости:

```gradle
testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
```

## CI/CD

Проект собирается через Jenkins (`Jenkinsfile`):

- checkout → `./gradlew build`
- прогон тестов
- сборка Docker-образа
- (опционально) push в registry

## Roadmap

- [ ] Добавить `@ControllerAdvice` для единой обработки ошибок
- [ ] Валидация DTO через `jakarta.validation` (`@Valid`, `@NotBlank`)
- [ ] JaCoCo для отчёта о покрытии
- [ ] WireMock-тесты для `YandexGptClient` и `YandexTranslateClient`
- [ ] Интеграционные тесты с Testcontainers (при появлении БД)
- [ ] Swagger/OpenAPI документация

## Лицензия

MIT.
