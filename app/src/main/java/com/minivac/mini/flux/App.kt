package com.minivac.mini.flux

import android.app.Application
import com.minivac.mini.BuildConfig
import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.dagger.AppModule
import com.minivac.mini.dagger.DaggerDefaultAppComponent
import com.squareup.leakcanary.LeakCanary
import mini.*
import mini.log.LoggerInterceptor
import mini.log.LogsController
import java.io.File
import kotlin.properties.Delegates

private var _app: App by Delegates.notNull()
private var _appComponent: AppComponent? = null
val app: App get() = _app
val appComponent: AppComponent get() = _appComponent!!

class App : Application() {

    val exceptionHandlers: MutableList<Thread.UncaughtExceptionHandler> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        _app = this
        if (BuildConfig.DEBUG) {
            Grove.plant(DebugTree(true))
        }

        _appComponent = DaggerDefaultAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
        val stores = appComponent.stores()
        val dispatcher = appComponent.dispatcher()
        dispatcher.addInterceptor(LoggerInterceptor(stores.values))

        val logsFolder = File(externalCacheDir, "logs")
        val logsController = LogsController(logsFolder)
        logsController.newFileLogWriter()?.run {
            Grove.plant(this)
        }

        dispatcher.actionReducers.add(MiniActionReducer(stores))
        dispatcher.actionReducers.add(DynamicActionReducer())
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

