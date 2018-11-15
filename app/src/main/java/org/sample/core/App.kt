package org.sample.core

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import mini.MiniActionReducer
import mini.initStores
import mini.log.DebugTree
import mini.log.Grove
import mini.log.LoggerInterceptor
import org.sample.BuildConfig
import org.sample.core.dagger.AppComponent
import org.sample.core.dagger.AppModule
import org.sample.core.dagger.DaggerAppComponent
import kotlin.properties.Delegates

private var appInstance: App by Delegates.notNull()
val app: App get() = appInstance

/**
 * Global application object.
 */
class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return component
    }

    val component: AppComponent
        get() {
            if (componentInstance == null) {
                componentInstance = DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()
            }
            return componentInstance!!
        }
    private var componentInstance: AppComponent? = null

    override fun onCreate() {
        appInstance = this
        super.onCreate()

        if (BuildConfig.DEBUG) Grove.plant(DebugTree(true))

        val stores = componentInstance!!.stores()
        componentInstance!!.dispatcher().addActionReducer(MiniActionReducer(stores = stores))
        componentInstance!!.dispatcher()
            .addInterceptor(LoggerInterceptor(stores = stores.values, logInBackground = false))

        initStores(componentInstance!!.stores().values)

        component.inject(this)
    }
}
