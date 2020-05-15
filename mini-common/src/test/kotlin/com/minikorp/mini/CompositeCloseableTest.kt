package com.minikorp.mini

import org.amshove.kluent.`should be equal to`
import org.junit.Test
import java.io.Closeable

class CompositeCloseableTest {

    @Test
    fun itemsAreClosed() {
        val c = CompositeCloseable()
        val dummyCloseable = DummyCloseable()
        c.add(dummyCloseable)
        c.close()
        c.close()

        dummyCloseable.closed.`should be equal to`(1)
    }

    class DummyCloseable : Closeable {
        var closed = 0
        override fun close() {
            closed++
        }
    }
}