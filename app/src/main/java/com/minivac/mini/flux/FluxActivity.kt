package com.minivac.mini.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.dagger.ComponentHolder
import javax.inject.Inject


abstract class FluxActivity<out T : Any> : AppCompatActivity(), ComponentHolder<T>{

    @Inject lateinit protected var dispatcher : Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null) app.registerComponent(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isFinishing) app.unregisterComponent(this)
    }
}