package mini

import java.io.Closeable

/**
 * A collection of closeables.
 */
class CompositeCloseable : Closeable {
    private val items = ArrayList<Closeable>()

    override fun close() {
        synchronized(this) {
            items.forEach { it.close() }
            items.clear()
        }
    }

    fun add(closeable: Closeable) {
        synchronized(this) {
            items.add(closeable)
        }
    }
}