package com.mini.android

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import mini.CloseableTracker
import mini.DefaultCloseableTracker

abstract class FluxViewModel : ViewModel(),
                               CloseableTracker by DefaultCloseableTracker() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        clearCloseables()
    }
}