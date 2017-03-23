package com.minivac.mini.log

import android.app.Application
import com.minivac.mini.dagger.AppScope
import com.minivac.mini.flux.Store
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import javax.inject.Inject


@AppScope
class LoggerStore @Inject constructor(val context: Application) : Store<LoggerState>() {
    override fun initialState() = LoggerState(FileLogController(context))
    override fun init() {

    }
}

data class LoggerState(val fileLogController: FileLogController)

@Module
abstract class LoggerStoreModule {
    @Binds @IntoMap @ClassKey(LoggerStore::class)
    abstract fun provideLoggerStore(store: LoggerStore): Store<*>
}