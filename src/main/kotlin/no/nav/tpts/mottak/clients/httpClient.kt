package no.nav.tpts.mottak.clients

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*

val httpClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.BODY
    }
    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
    }
}