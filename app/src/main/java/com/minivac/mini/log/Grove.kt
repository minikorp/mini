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
    private var forest: Array<Tree> = emptyArray()
    private var explicitTag = ThreadLocal<String>()

    fun plant(tree: Tree) {
        forest += tree
    }

    fun uproot(tree: Tree) {
        forest = forest.filter { it != tree }.toTypedArray()
    }

    fun tag(tag: String): Grove {
        explicitTag.set(tag)
        return this
    }

    fun v(throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(Log.VERBOSE, throwable, msg)
    }

    fun d(throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(Log.DEBUG, throwable, msg)
    }

    fun i(throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(Log.INFO, throwable, msg)
    }

    fun w(throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(Log.WARN, throwable, msg)
    }

    fun e(throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(Log.ERROR, throwable, msg)
    }

    fun wtf(throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(Log.ASSERT, throwable, msg)
    }

    fun log(priority: Int, throwable: Throwable? = null, msg: (() -> String)? = null) {
        doLog(priority, throwable, msg)
    }

    inline fun <T> timed(msg: String, fn: () -> T): T {
        val start = System.nanoTime()
        val r = fn()
        val elapsed = (System.nanoTime() - start) / 1000000
        i { "[$elapsed ms] - $msg" }
        return r
    }


    private fun doLog(priority: Int, throwable: Throwable?, msg: (() -> String)?) {
        val tag = consumeTag()
        if (msg == null && throwable == null) return
        var message: String? = null
        forest.forEach {
            if (!it.isLoggable(tag, priority)) return@forEach

            if (message == null) { //Only once
                if (msg == null) {
                    message = getStackTraceString(throwable!!)
                } else {
                    message = msg()
                    if (throwable != null) message += "\n${getStackTraceString(throwable)}"
                }
            }

            it.log(priority, tag, message.orEmpty())
        }
    }

    private fun consumeTag(): String {
        val tag = explicitTag.get()
        if (tag != null) {
            explicitTag.remove()
        }
        return tag ?: defaultTag
    }

    private fun getStackTraceString(t: Throwable): String {
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
        private const val CALL_STACK_INDEX = 5
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
    }
}