package com.minivac.mini.log

import android.support.v4.util.Pools
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.regex.Pattern

/** Logging for lazy people.
 * Adapted from Jake Wharton's Timber
 * [https://github.com/JakeWharton/timber]
 * */
object Grove {

    private val ANONYMOUS_CLASS_PATTERN = Pattern.compile("(\\$\\d+)+$")

    val defaultTag = "Grove"
    var debugTags = true
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
            return tag
        } else if (debugTags) {
            return createDebugTag(1)
        } else {
            return defaultTag
        }
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

    private fun createDebugTag(stackIndex: Int): String {
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= stackIndex) {
            throw IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?")
        }
        return createStackElementTag(stackTrace[stackIndex])
    }

    private fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS_PATTERN.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }
}

interface Tree {
    fun isLoggable(tag: String, priority: Int): Boolean = true
    fun log(priority: Int, tag: String, message: String)
}

/** A [Tree] for debug builds. Automatically infers the tag from the calling class.  */
class DebugTree : Tree {

    private val MAX_LOG_LENGTH = 4000

    /**
     * Break up `message` into maximum-length chunks (if needed) and send to either
     * [Log.println()][Log.println] or
     * [Log.wtf()][Log.wtf] for logging.

     * {@inheritDoc}
     */
    override fun log(priority: Int, tag: String, message: String) {
        if (message.length < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(tag, message)
            } else {
                Log.println(priority, tag, message)
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
                    Log.wtf(tag, part)
                } else {
                    Log.println(priority, tag, part)
                }
                i = end
            } while (i < newline)
            i++
        }
    }
}

/**
 * Logger that writes asynchronously to a file.
 * Automatically infers the tag from the calling class.
 */
class FileTree
/**
 * Create a new FileTree instance that will write in a background thread
 * any incoming logs as long as the level is at least `minLevel`.

 * @param file     The file this logger will write.
 * *
 * @param minLevel The minimum message level that will be written (inclusive).
 */
(val file: File, private val minLevel: Int) : Tree {
    private val queue = ArrayBlockingQueue<LogLine>(100)
    private val pool = Pools.SynchronizedPool<LogLine>(20)

    private val backgroundThread: Thread
    private val writer: Writer?

    init {
        var writer: Writer?
        this.backgroundThread = Thread(Runnable { this.loop() })
        try {
            //Not buffered, we want to write on the spot
            writer = FileWriter(file.absolutePath, true)
            this.backgroundThread.start()
        } catch (e: IOException) {
            writer = null
            Grove.e(e) { "Failed to create writer, nothing will be done" }
        }

        this.writer = writer
    }

    /**
     * Flush the file, this call is required before application dies or the file will be empty.
     */
    fun flush() {
        if (writer != null) {
            try {
                writer.flush()
            } catch (e: IOException) {
                Grove.e(e) { "Flush failed" }
            }

        }
    }

    override fun log(priority: Int, tag: String, message: String) {
        enqueueLog(priority, tag, message)
    }

    private fun enqueueLog(priority: Int, tag: String, message: String) {
        var logLine: LogLine? = pool.acquire()
        if (logLine == null) {
            logLine = LogLine()
        }

        logLine.tag = tag
        logLine.message = message
        logLine.level = priority
        logLine.date.time = System.currentTimeMillis()

        queue.offer(logLine)
    }

    private fun loop() {
        while (true) {
            try {
                val logLine = queue.take()
                if (writer != null) {
                    val lines = logLine.format()
                    for (line in lines) {
                        writer.write(line)
                    }
                }
                logLine.clear()
                pool.release(logLine)
            } catch (e: InterruptedException) {
                break //We are done
            } catch (e: IOException) {
                Grove.e(e) { "Failed to write line" }
                break
            }

        }
        closeSilently()
    }

    /**
     * Close the file and exit. This method does not block.
     */
    fun exit() {
        this.backgroundThread.interrupt()
    }

    private fun closeSilently() {
        if (writer != null) {
            try {
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    override fun toString(): String {
        return "FileTree{" +
                "file=" + file.absolutePath +
                '}'
    }

    private class LogLine {
        companion object {
            private val LOG_FILE_DATE_FORMAT = SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.US)
        }

        internal val date = Date()
        internal var level: Int = 0
        internal var message: String? = null
        internal var tag: String? = null

        internal fun clear() {
            message = null
            tag = null
            date.time = 0
            level = 0
        }

        internal fun format(): Array<String> {
            val lines = message!!.split("\n".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val levelString: String
            when (level) {
                Log.DEBUG -> levelString = "D"
                Log.INFO -> levelString = "I"
                Log.WARN -> levelString = "W"
                Log.ERROR -> levelString = "E"
                else -> levelString = "V"
            }

            //[29-04-1993 01:02:34.567 D/SomeTag: The value to Log]
            val prelude = String.format(Locale.US, "[%s] %s/%s: ", LOG_FILE_DATE_FORMAT.format(date), levelString, tag)
            for (i in lines.indices) {
                lines[i] = prelude + lines[i] + "\r\n"
            }
            return lines
        }
    }
}
