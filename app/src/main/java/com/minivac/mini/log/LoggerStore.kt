package com.minivac.mini.log

import android.app.Application
import mini.Grove
import com.minivac.mini.dagger.AppScope
import com.minivac.mini.flux.*
import com.minivac.mini.flux.OnActivityLifeCycleAction.ActivityStage.DESTROYED
import com.minivac.mini.flux.OnActivityLifeCycleAction.ActivityStage.STOPPED
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.Dispatcher
import mini.Store
import javax.inject.Inject


@AppScope
class LoggerStore @Inject constructor(
    context: Application,
    val dispatcher: Dispatcher,
    val lazyStoreMap: LazyStoreMap) : Store<LoggerState>() {

    private val fileLogController = FileLogController(context)

    override fun initialState() = LoggerState()

    override fun init() {
        val fileTree = fileLogController.newFileTree()
        if (fileTree != null) {
            Grove.plant(fileTree)
        }

        dispatcher.addInterceptor(LoggerInterceptor(lazyStoreMap.get().values))
        app.exceptionHandlers.add(Thread.UncaughtExceptionHandler { t, e ->
            fileTree?.flush()
        })
    }
}

class LoggerState

@Module
abstract class LoggerModule {
    @Binds @AppScope @IntoMap @ClassKey(LoggerStore::class)
    abstract fun storeToMap(store: LoggerStore): Store<*>
}