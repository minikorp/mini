package com.minivac.mini.log

import android.app.Application
import com.minivac.mini.dagger.AppScope
import com.minivac.mini.flux.Store
import javax.inject.Inject


@AppScope
class LoggerStore @Inject constructor(val context: Application) : Store<LoggerState>() {

    override fun initialState() = LoggerState(LogController(context))

    override fun init() {
    }
}


data class LoggerState(val logController: LogController)