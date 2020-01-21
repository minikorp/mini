package com.mini.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
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

    override val defaultDispatchScope: CoroutineScope
        get() = lifecycleScope


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { whenCreated(savedInstanceState) }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { whenResumed() }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch { whenPaused() }
    }

    override fun onDestroy() {
        lifecycleScope.launch { whenDestroyed() }
        clearCloseables()
        super.onDestroy()
    }

    fun <T> Flow<T>.launchOnUi() {
        launchIn(lifecycleScope)
    }

    protected open suspend fun whenCreated(savedInstanceState: Bundle?) = Unit
    protected open suspend fun whenResumed() = Unit
    protected open suspend fun whenPaused() = Unit
    protected open suspend fun whenDestroyed() = Unit

}