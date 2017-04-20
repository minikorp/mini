package com.minivac.mini.flux

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.minivac.mini.dagger.ComponentFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject


abstract class FluxActivity<T : Any> : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    abstract val componentFactory: ComponentFactory<T>
    @Inject lateinit protected var dispatcher: Dispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) app.registerComponent(componentFactory)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            //This won't be called if app is killed!
            app.unregisterComponent(componentFactory)
        }
    }

    fun <T : Disposable> T.track(): T {
        disposables.add(this)
        return this
    }
}