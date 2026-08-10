package de.mistatee.erglog.di

import de.mistatee.erglog.data.auth.AuthApi
import de.mistatee.erglog.data.auth.AuthRepository
import de.mistatee.erglog.data.auth.Concept2AuthRepository
import de.mistatee.erglog.data.auth.EncryptedSessionStore
import de.mistatee.erglog.data.auth.KtorAuthApi
import de.mistatee.erglog.data.auth.SessionStore
import de.mistatee.erglog.data.profile.Concept2ProfileRepository
import de.mistatee.erglog.data.profile.KtorProfileApi
import de.mistatee.erglog.data.profile.ProfileApi
import de.mistatee.erglog.data.defaultHttpClient
import de.mistatee.erglog.data.profile.ProfileRepository
import de.mistatee.erglog.ui.login.LoginScreenViewModel
import de.mistatee.erglog.ui.main.MainScreenViewModel
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single<HttpClient> { defaultHttpClient() }

        single<AuthApi> { KtorAuthApi(get()) }
        single<SessionStore> { EncryptedSessionStore(androidContext()) }
        single<AuthRepository> { Concept2AuthRepository(get(), get()) }

        single<ProfileApi> { KtorProfileApi(get()) }
        single<ProfileRepository> { Concept2ProfileRepository(get(), get()) }

        viewModel { LoginScreenViewModel(get()) }
        viewModel { MainScreenViewModel(get()) }
    }
