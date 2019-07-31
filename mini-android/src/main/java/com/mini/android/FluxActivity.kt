package com.mini.android

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import mini.CloseableTracker
import mini.DefaultCloseableTracker
import kotlin.coroutines.CoroutineContext

abstract class FluxActivity : AppCompatActivity(),
    CloseableTracker by DefaultCloseableTracker(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext
}