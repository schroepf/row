package de.mistatee.erglog.di

import de.mistatee.erglog.data.local.EncryptedPreferencesStore
import de.mistatee.erglog.data.local.ErgLogDatabase
import de.mistatee.erglog.data.local.LocalCache
import de.mistatee.erglog.data.local.LocalTransactionRunner
import de.mistatee.erglog.data.local.PreferencesStore
import de.mistatee.erglog.data.local.RoomLocalCache
import de.mistatee.erglog.data.local.RoomTransactionRunner
import de.mistatee.erglog.data.local.dao.ProfileDao
import de.mistatee.erglog.data.local.dao.ResultDao
import de.mistatee.erglog.data.local.dao.ResultSyncStateDao
import de.mistatee.erglog.data.remote.auth.Concept2RemoteAuthDataSource
import de.mistatee.erglog.data.remote.auth.RemoteAuthDataSource
import de.mistatee.erglog.data.remote.defaultHttpClient
import de.mistatee.erglog.data.remote.profile.Concept2RemoteProfileDataSource
import de.mistatee.erglog.data.remote.profile.RemoteProfileDataSource
import de.mistatee.erglog.data.remote.results.Concept2RemoteResultsDataSource
import de.mistatee.erglog.data.remote.results.RemoteResultsDataSource
import de.mistatee.erglog.data.repository.AuthRepository
import de.mistatee.erglog.data.repository.Concept2AuthRepository
import de.mistatee.erglog.data.repository.Concept2ProfileRepository
import de.mistatee.erglog.data.repository.Concept2ResultRepository
import de.mistatee.erglog.data.repository.ProfileRepository
import de.mistatee.erglog.data.repository.ResultRemoteMediator
import de.mistatee.erglog.data.repository.ResultRepository
import de.mistatee.erglog.ui.login.LoginScreenViewModel
import de.mistatee.erglog.ui.main.MainScreenViewModel
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.Clock

@OptIn(androidx.paging.ExperimentalPagingApi::class)
val appModule =
    module {
        single<HttpClient> { defaultHttpClient() }

        single<Clock> { Clock.System }

        single<ErgLogDatabase> { ErgLogDatabase.create(androidContext()) }
        single<LocalTransactionRunner> { RoomTransactionRunner(get<ErgLogDatabase>()) }
        single<LocalCache> { RoomLocalCache(get()) }
        single<ResultDao> { get<ErgLogDatabase>().resultDao() }
        single<ResultSyncStateDao> { get<ErgLogDatabase>().resultSyncStateDao() }
        single<ProfileDao> { get<ErgLogDatabase>().profileDao() }

        single<RemoteAuthDataSource> { Concept2RemoteAuthDataSource(get()) }
        single<PreferencesStore> { EncryptedPreferencesStore(androidContext()) }
        single<AuthRepository> { Concept2AuthRepository(get(), get(), get(), get()) }

        single<RemoteProfileDataSource> { Concept2RemoteProfileDataSource(get()) }
        single<ProfileRepository> { Concept2ProfileRepository(get(), get(), get()) }

        single<RemoteResultsDataSource> { Concept2RemoteResultsDataSource(get()) }
        single<ResultRemoteMediator> { ResultRemoteMediator(get(), get(), get(), get(), get()) }
        single<ResultRepository> { Concept2ResultRepository(get(), get()) }

        viewModel { LoginScreenViewModel(get()) }
        viewModel { MainScreenViewModel(get(), get()) }
    }
