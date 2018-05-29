package com.minivac.mini.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.dagger.inject
import mini.DefaultSubscriptionTracker
import mini.SubscriptionTracker

abstract class FluxActivity : AppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appComponent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}