package com.mini.android

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class FluxFragment : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    inline fun <T> Flow<T>.collectOnUi(crossinline fn: suspend (T) -> Unit) {
        this as LifecycleOwner
        launch {
            collect(fn)
        }
    }
}