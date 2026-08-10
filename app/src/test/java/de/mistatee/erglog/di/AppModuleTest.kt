package de.mistatee.erglog.di

import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class AppModuleTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appModule_verifiesSuccessfully() {
        appModule.verify(extraTypes = listOf(Context::class, HttpClientEngine::class))
    }
}
