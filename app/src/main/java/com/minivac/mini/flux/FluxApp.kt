package com.minivac.mini.flux

import android.app.Application
import com.minivac.mini.BuildConfig
import com.minivac.mini.dagger.*
import com.minivac.mini.log.DebugTree
import com.minivac.mini.log.Grove
import com.minivac.mini.misc.collectDeviceBuildInformation
import kotlin.properties.Delegates

private var _app: FluxApp by Delegates.notNull<FluxApp>()
val app: FluxApp get() = _app

class FluxApp : Application(), ComponentManager by DefaultComponentManager() {

    override fun onCreate() {
        super.onCreate()
        _app = this
        if (BuildConfig.DEBUG) {
            Grove.plant(DebugTree(true))
            Grove.d { collectDeviceBuildInformation(this) }
        }

        registerComponent(object : ComponentHolder<AppComponent> {
            override fun createComponent(): AppComponent {
                return DaggerAppComponent.builder()
                        .appModule(AppModule(app))
                        .build()
            }

            override val dependencies: List<String> = emptyList()
            override val componentName: String = AppComponent.NAME
        })
        val stores = AppComponent.get().stores()
        initStores(stores.values.toList())
        registerSystemCallbacks(AppComponent.get().dispatcher(), this)
    }
}

