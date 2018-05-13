package com.minivac.mini.dagger

import android.app.Application
import android.content.Context
import com.minivac.mini.flux.App
import com.minivac.mini.flux.StoreMap
import com.minivac.mini.log.LoggerModule
import dagger.Component
import dagger.Module
import dagger.Provides
import mini.Dispatcher
import org.sample.todo.MainActivity
import org.sample.todo.MightModule
import org.sample.todo.WarcraftModule

interface AppComponent {
    fun dispatcher(): Dispatcher
    fun stores(): StoreMap
}

@AppScope
@Component(modules = arrayOf(
    AppModule::class,
    WarcraftModule::class,
    MightModule::class,
    LoggerModule::class
))

interface DefaultAppComponent : AppComponent {
    fun inject(target: MainActivity)
}
@Module
class AppModule(val app: App) {
    @Provides @AppScope fun provideDispatcher() = Dispatcher()
    @Provides fun provideApplication(): Application = app
    @Provides fun provideAppContext(): Context = app
}