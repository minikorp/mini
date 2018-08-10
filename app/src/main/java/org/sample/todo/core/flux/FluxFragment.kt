package org.sample.todo.core.flux

import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import mini.DefaultSubscriptionTracker
import mini.Dispatcher
import mini.SubscriptionTracker
import javax.inject.Inject

open class FluxFragment :
    Fragment(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject
    lateinit var dispatcher: Dispatcher

    @Suppress("KDocMissingDocumentation")
    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        cancelSubscriptions()
    }
}