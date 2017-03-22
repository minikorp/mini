package com.minivac.mini

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.flux.Action
import com.minivac.mini.log.DebugTree
import com.minivac.mini.log.Grove

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Grove.plant(DebugTree())
    }

    data class DummyAction(val x: Int = 3) : Action
}