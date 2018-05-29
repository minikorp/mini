package com.minivac.mini.dagger

import android.app.Application
import android.content.Context
import com.minivac.mini.flux.App
import mini.StoreMap
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
@Component(modules = [(AppModule::class), (WarcraftModule::class), (MightModule::class)])

interface DefaultAppComponent : AppComponent {
    fun inject(target: MainActivity)
}
@Module
class AppModule(val app: App) {
    @Provides @AppScope fun provideDispatcher() = Dispatcher()
    @Provides fun provideApplication(): Application = app
    @Provides fun provideAppContext(): Context = app
}