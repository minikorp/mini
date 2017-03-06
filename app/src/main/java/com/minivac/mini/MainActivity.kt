package com.minivac.mini

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.flux.Action
import com.minivac.mini.flux.Dispatcher
import com.minivac.mini.log.DebugTree
import com.minivac.mini.log.Grove
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dummyAction = DummyAction()
        dummyAction.tags

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

        try {
            throw NullPointerException()
        } catch (e: Exception) {
            Grove.e(e)
        }

        Grove.plant(DebugTree())

        Grove.timed("something") {
            Grove.v { "Debug" }
            Grove.d { "Debug" }
            Grove.i { "Debug" }
            Grove.w { "Debug" }
            Grove.e { "Debug" }
            Grove.wtf { "Debug" }
        }
    }

    class DummyAction : Action
}
