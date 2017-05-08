package com.minivac.mini.dagger

import android.app.Application
import android.content.Context
import com.minivac.mini.flux.App
import com.minivac.mini.flux.Dispatcher
import com.minivac.mini.flux.StoreHolderComponent
import com.minivac.mini.log.LoggerModule
import dagger.Component
import dagger.Module
import dagger.Provides
import org.sample.todo.UserComponent


@Component(modules = arrayOf(
        AppModule::class,
        LoggerModule::class
))
@AppScope
interface AppComponent : StoreHolderComponent {
    fun dispatcher(): Dispatcher

    fun mainActivityComponent(): UserComponent
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