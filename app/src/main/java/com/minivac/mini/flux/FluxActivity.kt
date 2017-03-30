package com.minivac.mini.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.dagger.ComponentFactory
import javax.inject.Inject


abstract class FluxActivity<out T : Any> : AppCompatActivity() {

    abstract val componentFactory: ComponentFactory<T>
    @Inject lateinit protected var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) app.registerComponent(componentFactory)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            //This won't be called if app is killed!
            app.unregisterComponent(componentFactory)
        }
    }
}