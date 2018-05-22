package com.minivac.mini.rx

import android.app.Activity
import android.support.annotation.CallSuper
import mini.DefaultSubscriptionTracker
import mini.SubscriptionTracker

abstract class RxActivity : Activity(), SubscriptionTracker by DefaultSubscriptionTracker() {

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}