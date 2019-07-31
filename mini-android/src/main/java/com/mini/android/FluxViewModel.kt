package com.mini.android

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import mini.CompositeCloseable
import java.io.Closeable

abstract class FluxViewModel : ViewModel() {

    private val compositeCloseable = CompositeCloseable()

    fun <T : Closeable> T.track(): T {
        compositeCloseable.add(this)
        return this
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        compositeCloseable.close()
    }
}