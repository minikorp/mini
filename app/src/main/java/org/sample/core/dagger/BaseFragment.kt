package org.sample.core.dagger

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import dagger.android.support.DaggerFragment
import mini.DefaultSubscriptionTracker
import mini.SubscriptionTracker

/** Base [Fragment] to use with Flux+Dagger in the app. */
@SuppressLint("Registered")
abstract class BaseFragment :
    DaggerFragment(),
    SubscriptionTracker by DefaultSubscriptionTracker(){

    override fun onDestroyView() {
        super.onDestroyView()
        cancelSubscriptions()
    }
}