package de.mistatee.erglog

import android.app.Application
import de.mistatee.erglog.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ErgLogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ErgLogApplication)
            modules(appModule)
        }
    }
}
