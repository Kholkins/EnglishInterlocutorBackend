package org.kholkins.englishinterlocutorbackend.framework

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EnglishInterlocutorBackendApplication

fun main(args: Array<String>) {
    runApplication<EnglishInterlocutorBackendApplication>(*args)
}
