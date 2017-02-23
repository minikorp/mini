package com.minivac.mini.log

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

/** Logging for lazy people.
 * Adapted from Jake Wharton's Timber
 * [https://github.com/JakeWharton/timber]
 * */
object Grove {

    val defaultTag = "Grove"
    var forest: Array<Tree> = emptyArray()
    private var explicitTag = ThreadLocal<String>()

    fun plant(tree: Tree) {
        forest += tree
    }

    fun uproot(tree: Tree) {
        forest = forest.filter { it != tree }.toTypedArray()
    }

    inline fun v(throwable: Throwable? = null, msg: (() -> String)) {
        log(Log.VERBOSE, throwable, msg)
    }

    inline fun d(throwable: Throwable? = null, msg: (() -> String)) {
        log(Log.DEBUG, throwable, msg)
    }

    inline fun i(throwable: Throwable? = null, msg: (() -> String)) {
        log(Log.INFO, throwable, msg)
    }

    inline fun w(throwable: Throwable? = null, msg: (() -> String)) {
        log(Log.WARN, throwable, msg)
    }

    inline fun e(throwable: Throwable? = null, msg: (() -> String)) {
        log(Log.ERROR, throwable, msg)
    }

    inline fun wtf(throwable: Throwable? = null, msg: (() -> String)) {
        log(Log.ASSERT, throwable, msg)
    }

    inline fun <T> timed(msg: String, fn: () -> T): T {
        val start = System.nanoTime()
        val r = fn()
        val elapsed = (System.nanoTime() - start) / 1000000
        i { "[$elapsed ms] - $msg" }
        return r
    }

    inline fun log(priority: Int, throwable: Throwable? = null, msg: (() -> String)) {
        val tag = consumeTag()
        var message: String? = null //Lazy evaluation
        for (tree in forest) {
            if (!tree.isLoggable(tag, priority)) continue
            if (message == null) {
                message = msg()
                if (throwable != null) message += "\n${getStackTraceString(throwable)}"
            }
            tree.log(priority, tag, message.orEmpty())
        }
    }

    fun tag(tag: String): Grove {
        explicitTag.set(tag)
        return this
    }

    fun consumeTag(): String {
        val tag = explicitTag.get()
        if (tag != null) {
            explicitTag.remove()
        }
        return tag ?: defaultTag
    }

    fun getStackTraceString(t: Throwable): String {
        // Don't replace this with Log.getStackTraceString() - it hides
        // UnknownHostException, which is not what we want.
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}

interface Tree {
    fun isLoggable(tag: String, priority: Int): Boolean = true
    fun log(priority: Int, tag: String, message: String)
}

/** A [Tree] for debug builds. Automatically infers the tag from the calling class.  */
class DebugTree : Tree {

    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }

    /**
     * Break up `message` into maximum-length chunks (if needed) and send to either
     * [Log.println()][Log.println] or
     * [Log.wtf()][Log.wtf] for logging.

     * {@inheritDoc}
     */
    override fun log(priority: Int, tag: String, message: String) {
        var debugTag = tag
        if (debugTag == Grove.defaultTag) {
            val stackTrace = Throwable().stackTrace
            if (stackTrace.size <= CALL_STACK_INDEX) {
                throw IllegalStateException(
                        "Synthetic stacktrace didn't have enough elements: are you using proguard?")
            }
            debugTag = createStackElementTag(stackTrace[CALL_STACK_INDEX])
        }

        if (message.length < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(debugTag, message)
            } else {
                Log.println(priority, debugTag, message)
            }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                if (priority == Log.ASSERT) {
                    Log.wtf(debugTag, part)
                } else {
                    Log.println(priority, debugTag, part)
                }
                i = end
            } while (i < newline)
            i++
        }
    }

    companion object {
        private const val MAX_LOG_LENGTH = 4000
        private const val CALL_STACK_INDEX = 1
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    }
}