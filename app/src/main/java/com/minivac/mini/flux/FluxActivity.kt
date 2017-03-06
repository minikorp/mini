package com.minivac.mini.flux

import android.app.Activity
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class FluxActivity : Activity() {

    private val disposables = CompositeDisposable()

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    fun Disposable.track() {
        disposables.add(this)
    }
}