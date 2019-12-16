package com.mini.android

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import mini.CloseableTracker
import mini.DefaultCloseableTracker
import mini.DispatcherContainer
import kotlin.coroutines.CoroutineContext

abstract class FluxActivity : AppCompatActivity(),
                              CloseableTracker by DefaultCloseableTracker(),
                              CoroutineScope,
                              DispatcherContainer {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    override val defaultDispatchScope: CoroutineScope get() = lifecycleScope

}