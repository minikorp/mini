package com.minivac.mini.flux

import android.app.Application
import com.minivac.mini.BuildConfig
import com.minivac.mini.dagger.*
import mini.DebugTree
import mini.Grove
import com.squareup.leakcanary.LeakCanary
import kotlin.properties.Delegates

private var _app: App by Delegates.notNull<App>()
val app: App get() = _app

class App :
        Application(),
        ComponentManager by DefaultComponentManager() {

    val exceptionHandlers: MutableList<Thread.UncaughtExceptionHandler> = ArrayList()

    override fun onCreate() {
        super.onCreate()
        _app = this
        if (BuildConfig.DEBUG) {
            Grove.plant(DebugTree(true))
        }

        registerComponent(object : ComponentFactory<AppComponent> {
            override fun createComponent(): AppComponent {
                return DaggerAppComponent.builder()
                        .appModule(AppModule(app))
                        .build()
            }

            override val componentType = AppComponent::class
        })

        val appComponent = findComponent(AppComponent::class)
        val stores = appComponent.stores()
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

