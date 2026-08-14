package de.mistatee.erglog.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


fun MockEngine.toHttpClient(): HttpClient =
    HttpClient(this) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
