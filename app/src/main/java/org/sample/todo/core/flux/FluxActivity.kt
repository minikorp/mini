package org.sample.todo.core.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.sample.todo.core.dagger.inject
import org.sample.todo.core.appComponent
import mini.DefaultSubscriptionTracker
import mini.Dispatcher
import mini.SubscriptionTracker
import javax.inject.Inject

abstract class FluxActivity : AppCompatActivity(),
    SubscriptionTracker by DefaultSubscriptionTracker() {

    @Inject
    protected lateinit var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appComponent, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}