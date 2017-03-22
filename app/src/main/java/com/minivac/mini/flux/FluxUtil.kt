package com.minivac.mini.flux

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.minivac.mini.log.Grove
import java.util.*

/**
 * Sort and create Stores initial state.
 */
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

/**
 * Register callbacks to send [OnTrimMemoryAction] and [OnActivityLifeCycle].
 */
fun registerSystemCallbacks(dispatcher: Dispatcher, context: Context) {
    val app = context.applicationContext as? Application

    app?.registerComponentCallbacks(object : ComponentCallbacks2 {
        override fun onLowMemory() {}

        override fun onConfigurationChanged(newConfig: Configuration?) {}

        override fun onTrimMemory(level: Int) {
            dispatcher.dispatch(OnTrimMemoryAction(level))
        }
    })

    app?.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?)
                = dispatcher.dispatch(OnActivityLifeCycle(activity, OnActivityLifeCycle.ActivityStage.CREATED))

        override fun onActivityStarted(activity: Activity)
                = dispatcher.dispatch(OnActivityLifeCycle(activity, OnActivityLifeCycle.ActivityStage.STARTED))

        override fun onActivityResumed(activity: Activity)
                = dispatcher.dispatch(OnActivityLifeCycle(activity, OnActivityLifeCycle.ActivityStage.RESUMED))

        override fun onActivityPaused(activity: Activity)
                = dispatcher.dispatch(OnActivityLifeCycle(activity, OnActivityLifeCycle.ActivityStage.PAUSED))

        override fun onActivityStopped(activity: Activity)
                = dispatcher.dispatch(OnActivityLifeCycle(activity, OnActivityLifeCycle.ActivityStage.STOPPED))

        override fun onActivityDestroyed(activity: Activity)
                = dispatcher.dispatch(OnActivityLifeCycle(activity, OnActivityLifeCycle.ActivityStage.DESTROYED))

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
    })
}