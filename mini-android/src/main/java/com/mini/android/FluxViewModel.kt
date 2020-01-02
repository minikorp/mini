package com.mini.android

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import mini.CloseableTracker
import mini.DefaultCloseableTracker
import mini.StateContainer

abstract class FluxViewModel : ViewModel(),
                               CloseableTracker by DefaultCloseableTracker() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clearCloseables()
    }
}

fun <T> LiveData<T>.asStateContainer(defaultValue: T? = null): StateContainer<T> {
    return object : StateContainer<T> {
        override val state: T
            get() = value ?: defaultValue
            ?: throw NullPointerException("No default value provided.")
    }
}