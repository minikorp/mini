package org.sample.todo.core

import android.app.Application
import com.minivac.mini.BuildConfig
import org.sample.todo.core.dagger.AppComponent
import org.sample.todo.core.dagger.AppModule
import org.sample.todo.core.dagger.DaggerDefaultAppComponent
import mini.DebugTree
import mini.Grove
import mini.MiniActionReducer
import mini.log.LoggerInterceptor
import org.jetbrains.annotations.TestOnly

private var _app: App? = null

//private var _prefs: Prefs by Delegates.notNull()
//private var _gson: Gson by Delegates.notNull()
private var _appComponent: AppComponent? = null
val app: App get() = _app!!
//val prefs: Prefs get() = _prefs
val appComponent: AppComponent get() = _appComponent!!

@TestOnly
fun setAppComponent(component: AppComponent) {
    _appComponent = component
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        _app = this

        if (BuildConfig.DEBUG) Grove.plant(DebugTree(true))


        if (_appComponent == null) {
            _appComponent = DaggerDefaultAppComponent
                    .builder()
                    .appModule(AppModule(this))
                    .build()
            _appComponent!!.dispatcher().addActionReducer(MiniActionReducer(stores = _appComponent!!.stores()))
            _appComponent!!.dispatcher().addInterceptor(LoggerInterceptor(_appComponent!!.stores().values))
        }
    }
}