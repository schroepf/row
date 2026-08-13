package de.mistatee.erglog.di

import de.mistatee.erglog.data.concept2.auth.api.AuthApi
import de.mistatee.erglog.data.concept2.auth.AuthRepository
import de.mistatee.erglog.data.concept2.auth.Concept2AuthRepository
import de.mistatee.erglog.data.concept2.auth.store.EncryptedSessionStore
import de.mistatee.erglog.data.concept2.auth.api.Concept2AuthApi
import de.mistatee.erglog.data.concept2.auth.store.SessionStore
import de.mistatee.erglog.data.concept2.logbook.profile.Concept2ProfileRepository
import de.mistatee.erglog.data.concept2.logbook.profile.api.Concept2ProfileApi
import de.mistatee.erglog.data.concept2.logbook.profile.api.ProfileApi
import de.mistatee.erglog.data.defaultHttpClient
import de.mistatee.erglog.data.concept2.logbook.profile.ProfileRepository
import de.mistatee.erglog.data.concept2.logbook.results.Concept2ResultRepository
import de.mistatee.erglog.data.concept2.logbook.results.api.Concept2ResultsApi
import de.mistatee.erglog.data.concept2.logbook.results.ResultRepository
import de.mistatee.erglog.data.concept2.logbook.results.api.ResultsApi
import de.mistatee.erglog.ui.login.LoginScreenViewModel
import de.mistatee.erglog.ui.main.MainScreenViewModel
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock

val appModule =
    module {
        single<HttpClient> { defaultHttpClient() }

        single<Clock> { Clock.System }

        single<AuthApi> { Concept2AuthApi(get()) }
        single<SessionStore> { EncryptedSessionStore(androidContext()) }
        single<AuthRepository> { Concept2AuthRepository(get(), get(), get()) }

        single<ProfileApi> { Concept2ProfileApi(get()) }
        single<ProfileRepository> { Concept2ProfileRepository(get(), get()) }

        single<ResultsApi> { Concept2ResultsApi(get()) }
        single<ResultRepository> { Concept2ResultRepository(get(), get()) }

        viewModel { LoginScreenViewModel(get()) }
        viewModel { MainScreenViewModel(get(), get()) }
    }
