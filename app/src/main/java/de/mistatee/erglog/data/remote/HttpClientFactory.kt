package de.mistatee.erglog.data.remote

import de.mistatee.erglog.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun defaultHttpClient(): HttpClient =
    HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.BODY
            }
        }
    }
