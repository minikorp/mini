package com.minikorp.mini.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.minikorp.mini.CloseableTracker
import com.minikorp.mini.DefaultCloseableTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class FluxFragment : Fragment(),
        CloseableTracker by DefaultCloseableTracker(),
        CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = lifecycleScope.coroutineContext

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        close()
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