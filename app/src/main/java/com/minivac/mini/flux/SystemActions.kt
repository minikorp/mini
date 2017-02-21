package com.minivac.mini.flux

import android.app.Activity


data class OnTrimMemoryAction(val level: Int) : Action

data class OnActivityLifeCycle(val activity: Activity, val stage: ActivityStage) : Action {
    enum class ActivityStage {
        PRE_ON_CREATE,
        CREATED,
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED,
        RESTARTED,
        DESTROYED
    }
}

