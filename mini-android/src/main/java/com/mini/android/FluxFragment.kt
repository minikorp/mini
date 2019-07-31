package com.mini.android

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import mini.CloseableTracker
import mini.DefaultCloseableTracker
import kotlin.coroutines.CoroutineContext

abstract class FluxFragment : Fragment(),
    CloseableTracker by DefaultCloseableTracker(),
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext
}