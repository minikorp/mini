package com.minivac.mini.flux

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Looper
import android.support.annotation.CallSuper
import com.minivac.mini.BuildConfig
import com.minivac.mini.flux.OnActivityLifeCycle.ActivityStage.*
import com.minivac.mini.log.DebugTree
import com.minivac.mini.log.Grove
import com.minivac.mini.misc.collectDeviceBuildInformation
import java.util.*

abstract class FluxApp : Application() {

    protected lateinit var stores: List<Store<*>>

    abstract fun createStores(): List<Store<*>>

    @CallSuper
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Grove.plant(DebugTree())
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?)
                    = Dispatcher.dispatch(OnActivityLifeCycle(activity, CREATED))

            override fun onActivityStarted(activity: Activity)
                    = Dispatcher.dispatch(OnActivityLifeCycle(activity, STARTED))

            override fun onActivityResumed(activity: Activity)
                    = Dispatcher.dispatch(OnActivityLifeCycle(activity, RESUMED))

            override fun onActivityPaused(activity: Activity)
                    = Dispatcher.dispatch(OnActivityLifeCycle(activity, PAUSED))

            override fun onActivityStopped(activity: Activity)
                    = Dispatcher.dispatch(OnActivityLifeCycle(activity, STOPPED))

            override fun onActivityDestroyed(activity: Activity)
                    = Dispatcher.dispatch(OnActivityLifeCycle(activity, DESTROYED))

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
        })

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Grove.tag("Fatal").e(throwable) {
                "UncaughtException, thread: ${thread.id}, " +
                        "main thread: ${thread == Looper.getMainLooper().thread}"
            }
            defaultHandler.uncaughtException(thread, throwable)
        }

        stores = createStores()
        initStores(stores)
    }

    @CallSuper
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Dispatcher.dispatch(OnTrimMemoryAction(level))
    }

    fun initStores(stores: List<Store<*>>) {
        val now = System.currentTimeMillis()

        Collections.sort(stores) { o1, o2 ->
            Integer.compare(
                    o1.properties.initOrder,
                    o2.properties.initOrder)
        }

        val initTimes = LongArray(stores.size)

        for (i in 0..stores.size - 1) {
            val start = System.currentTimeMillis()
            stores[i].init()
            stores[i].state //Create initial state
            initTimes[i] += System.currentTimeMillis() - start
        }

        Grove.d { collectDeviceBuildInformation(this) }
        val elapsed = System.currentTimeMillis() - now

        Grove.d { "┌ Application with ${stores.size} stores loaded in $elapsed ms" }
        Grove.d { "├────────────────────────────────────────────" }
        for (i in 0..stores.size - 1) {
            val store = stores[i]
            var boxChar = "├"
            if (store === stores[stores.size - 1]) {
                boxChar = "└"
            }
            Grove.d { "$boxChar ${store.javaClass.simpleName} - ${initTimes[i]} ms" }
        }
    }
}