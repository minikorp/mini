package old

import com.minikorp.mini.CompositeCloseable
import java.io.Closeable

interface CloseableTracker : Closeable {
    /**
     * Start tracking a disposable.
     */
    fun <T : Closeable> T.track(): T
}

class DefaultCloseableTracker : CloseableTracker {
    private val closeables = CompositeCloseable()
    override fun close() = closeables.close()
    override fun <T : Closeable> T.track(): T {
        closeables.add(this)
        return this
    }
}
