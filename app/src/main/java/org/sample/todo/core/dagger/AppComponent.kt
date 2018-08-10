package org.sample.todo.core.dagger

import android.app.Application
import android.content.Context
import org.sample.todo.MainActivity
import org.sample.todo.SecondActivity
import org.sample.todo.TestModule
import org.sample.todo.core.flux.FluxActivity
import org.sample.todo.core.flux.FluxFragment
import dagger.Component
import dagger.Module
import dagger.Provides
import org.sample.todo.core.App
import mini.Dispatcher
import mini.StoreMap

interface AppComponent {
    fun dispatcher(): Dispatcher
    fun stores(): StoreMap
}

@AppScope
@Component(modules = [
    AppModule::class,
    TestModule::class])

interface DefaultAppComponent : AppComponent {
    fun inject(target: FluxActivity)
    fun inject(target: FluxFragment)
    fun inject(target: MainActivity)
    fun inject(target: SecondActivity)
}

@Module
class AppModule(val app: App) {
    @Provides
    @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app
}