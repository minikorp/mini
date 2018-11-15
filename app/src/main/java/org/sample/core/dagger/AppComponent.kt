package org.sample.core.dagger

import android.app.Application
import android.content.Context
import com.frangsierra.larpy.core.dagger.ActivityScope
import com.frangsierra.larpy.core.dagger.AppScope
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import mini.Dispatcher
import org.sample.HomeActivity
import org.sample.LoginActivity
import org.sample.core.App
import org.sample.core.flux.StoreHolderComponent
import org.sample.session.store.SessionModule

/**
 * Main Dagger app component.
 */
@Component(
    modules = [
        AndroidInjectionModule::class,
        ActivityBindingsModule::class,
        AndroidSupportInjectionModule::class,
        SessionModule::class,
        AppModule::class
    ]
)
@AppScope
@Suppress("UndocumentedPublicFunction")
interface AppComponent : StoreHolderComponent, AndroidInjector<App> {
    fun dispatcher(): Dispatcher
}

@Module
@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
interface ActivityBindingsModule {

    @ActivityScope
    @ContributesAndroidInjector
    fun loginActivity(): LoginActivity

    @ActivityScope
    @ContributesAndroidInjector
    fun homeActivity(): HomeActivity
}

@Module
@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
class AppModule(val app: App) {

    @Provides
    @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app
}