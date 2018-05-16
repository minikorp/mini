package com.minivac.mini.flux

import android.app.Application
import com.minivac.mini.BuildConfig
import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.dagger.AppModule
import com.minivac.mini.dagger.DaggerDefaultAppComponent
import com.squareup.leakcanary.LeakCanary
import mini.DebugTree
import mini.Dispatcher
import mini.Grove
import mini.MiniActionReducer
import kotlin.properties.Delegates

private var _app: App by Delegates.notNull()
private var _dispatcher: Dispatcher by Delegates.notNull()
private var _appComponent: AppComponent? = null
val app: App get() = _app
val dispatcher: Dispatcher get() = _dispatcher
val appComponent: AppComponent get() = _appComponent!!

class App : Application() {

    val exceptionHandlers: MutableList<Thread.UncaughtExceptionHandler> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        _app = this
        _dispatcher = Dispatcher()
        if (BuildConfig.DEBUG) {
            Grove.plant(DebugTree(true))
        }

        _appComponent = DaggerDefaultAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
        val stores = appComponent.stores()
        _dispatcher.actionReducer = MiniActionReducer(stores)
        initStores(stores.values.toList())

        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        exceptionHandlers.add(exceptionHandler)
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            exceptionHandlers.forEach { it.uncaughtException(thread, error) }
        }
        configureLeakCanary()
    }

    private fun configureLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)
    }

}

