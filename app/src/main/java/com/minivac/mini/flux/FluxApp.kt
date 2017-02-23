package com.minivac.mini.flux

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.annotation.CallSuper
import com.minivac.mini.BuildConfig
import com.minivac.mini.flux.OnActivityLifeCycle.ActivityStage.*
import com.minivac.mini.log.DebugTree
import com.minivac.mini.log.Grove

abstract class FluxApp : Application() {

    @CallSuper
    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG){
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
    }

    @CallSuper
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Dispatcher.dispatch(OnTrimMemoryAction(level))
    }
}