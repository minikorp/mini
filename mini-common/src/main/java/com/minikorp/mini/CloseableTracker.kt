package com.minikorp.mini

import java.io.Closeable

interface CloseableTracker {
    /**
     * Clear all closeables.
     */
    fun clearCloseables()

    /**
     * Start tracking a disposable.
     */
    fun <T : Closeable> T.track(): T
}

class DefaultCloseableTracker : CloseableTracker {
    private val closeables = CompositeCloseable()
    override fun clearCloseables() = closeables.close()
    override fun <T : Closeable> T.track(): T {
        closeables.add(this)
        return this
    }
}
