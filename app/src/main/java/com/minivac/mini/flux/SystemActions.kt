package com.minivac.mini.flux

import android.app.Activity
import com.example.mini_commons.Action


data class OnTrimMemoryAction(val level: Int) : Action

data class OnActivityLifeCycleAction(val activity: Activity, val stage: ActivityStage) : Action {
    enum class ActivityStage {
        CREATED,
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED,
        RESTARTED,
        DESTROYED,
    }
}

