package com.minivac.mini.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.dagger.inject
import com.minivac.mini.rx.DefaultSubscriptionTracker
import com.minivac.mini.rx.SubscriptionTracker
import mini.Dispatcher
import javax.inject.Inject

abstract class FluxActivity : AppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject
    lateinit protected var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appComponent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}