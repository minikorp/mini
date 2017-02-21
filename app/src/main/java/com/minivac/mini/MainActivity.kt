package com.minivac.mini

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.flux.Action
import com.minivac.mini.flux.Dispatcher
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dummyAction = DummyAction()
        dummyAction.tags
        Timber.d("TAGS: %s", Arrays.toString(dummyAction.tags))

        Dispatcher.subscribe(tag = DummyAction::class, fn = { a ->
            a.tags;
        })

        Dispatcher.subscribeFlowable(tag = Any::class, fn = {
            it.observeOn(Schedulers.io())
                    .filter { it == null }
                    .subscribe()
        })

        Dispatcher.subscribeFlowable(tag = DummyAction::class) {
            it.filter { Any::class.java in it.tags }.subscribe {

            }.dispose()
        }.dispose()

        Dispatcher.dispatch(dummyAction)
    }

    class DummyAction : Action
}
