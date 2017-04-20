package com.minivac.mini.dagger

import android.app.Application
import android.content.Context
import com.minivac.mini.flux.App
import com.minivac.mini.flux.Dispatcher
import com.minivac.mini.flux.StoreMap
import com.minivac.mini.flux.app
import com.minivac.mini.log.LoggerModule
import dagger.Component
import dagger.Module
import dagger.Provides


@Component(modules = arrayOf(
        AppModule::class,
        LoggerModule::class
))
@AppScope
interface AppComponent {
    companion object {
        const val NAME = "AppComponent"
        fun get(): AppComponent = app.findComponent(AppComponent::class)
    }

    fun dispatcher(): Dispatcher

    fun stores(): StoreMap
}

@Module
class AppModule(val app: App) {
    @Provides @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app
}