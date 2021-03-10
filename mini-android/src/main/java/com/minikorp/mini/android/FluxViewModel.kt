package com.minikorp.mini.android

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.minikorp.mini.CloseableTracker
import com.minikorp.mini.DefaultCloseableTracker
import com.minikorp.mini.TypedStore
import com.minikorp.mini.assertOnUiThread
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

abstract class FluxViewModel<S : Any>(
        val savedStateHandle: SavedStateHandle) :
        ViewModel(),
        TypedStore<S>,
        CloseableTracker by DefaultCloseableTracker() {


    class ViewModelSubscription internal constructor(private val vm: FluxViewModel<*>,
                                                     private val fn: Any) : Closeable {
        override fun close() {
            vm.listeners.remove(fn)
        }
    }

    private var _state: Any? = TypedStore.Companion.NoState
    private val listeners = CopyOnWriteArrayList<(S) -> Unit>()

    override val state: S
        get() {
            if (_state === TypedStore.Companion.NoState) {
                synchronized(this) {
                    if (_state === TypedStore.Companion.NoState) {
                        _state = restoreState(savedStateHandle) ?: initialState()
                    }
                }
            }
            @Suppress("UNCHECKED_CAST")
            return _state as S
        }


    override fun setState(newState: S) {
        assertOnUiThread()
        performStateChange(newState)
    }

    private fun performStateChange(newState: S) {
        if (_state != newState) {
            _state = newState
            saveState(newState, savedStateHandle)
            listeners.forEach {
                it(newState)
            }
        }
    }

    /**
     * Persist the state, no-op by default.
     *
     * ```handle.set("state", state)```
     */
    open fun saveState(state: S, handle: SavedStateHandle) {
        //No-op
    }

    /**
     * Restore the state from the [SavedStateHandle] or null if nothing was saved.
     *
     * ```handle.get<S>("state")```
     */
    open fun restoreState(handle: SavedStateHandle): S? {
        return null
    }

    override fun subscribe(hotStart: Boolean, fn: (S) -> Unit): Closeable {
        listeners.add(fn)
        if (hotStart) fn(state)
        return ViewModelSubscription(this, fn)
    }

    override fun onCleared() {
        super.onCleared()
        close()
    }
}