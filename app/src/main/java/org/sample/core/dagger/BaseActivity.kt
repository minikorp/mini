package org.sample.core.dagger

import android.annotation.SuppressLint
import dagger.android.support.DaggerAppCompatActivity
import mini.DefaultSubscriptionTracker
import mini.SubscriptionTracker

/** Base [Activity] to use with Flux+Dagger in the app. */
@SuppressLint("Registered")
abstract class BaseActivity :
    DaggerAppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}