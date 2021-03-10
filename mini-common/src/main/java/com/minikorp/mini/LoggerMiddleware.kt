package com.minikorp.mini

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/** Actions implementing this interface won't log anything, including nested calls */
interface SilentAction

interface CompactLogAction

interface MotherAction

internal fun extractClassName(clazz: Class<*>): String {
    return clazz.name.substringAfterLast(".")
}

interface LoggerMiddlewareWriter {
    fun log(priority: Int, tag: String, message: String)
}

object AndroidLoggerMiddlewareWriter : LoggerMiddlewareWriter {
    override fun log(priority: Int, tag: String, message: String) {
        Log.println(priority, tag, message)
    }
}

/**
 * Action logging for stores.
 */
class LoggerMiddleware<S : Any>(private val tag: String = "MiniLog",
                                private val diffFunction: ((a: Any?, b: Any?) -> String)? = null,
                                private val writer: LoggerMiddlewareWriter = AndroidLoggerMiddlewareWriter) : Middleware<S> {

    private var actionCounter = AtomicInteger(0)

    override suspend fun intercept(context: DispatchContext<S>, chain: Chain<S>) {
        val action = context.action
        val store = context.store

        if (action is SilentAction) {
            return chain.proceed(context)
        } //Do nothing

        val stores = listOf(store)

        val isMother = action is MotherAction
        val isCompact = action is CompactLogAction
        val beforeStates: Array<Any?> = Array(stores.size) { }
        val afterStates: Array<Any?> = Array(stores.size) { }
        val actionName = extractClassName(action.javaClass)


        val prelude = "[${"${actionCounter.getAndIncrement() % 100}".padStart(2, '0')}] "
        if (isCompact) {
            writer.log(Log.DEBUG, tag, "$prelude#─> $action")
            return chain.proceed(context)
        }

        val (upCorner, downCorner) = if (isMother) {
            "╔═════ " to "╚════> "
        } else {
            "┌── " to "└─> "
        }
        val verticalBar = if (isMother) "║" else "│"
        writer.log(Log.DEBUG, tag, "$prelude$upCorner $actionName")
        writer.log(Log.DEBUG, tag, "$prelude$verticalBar $action")

        //Pass it down
        stores.forEachIndexed { idx, s -> beforeStates[idx] = s.state }
        val start = System.nanoTime()
        chain.proceed(context)
        val processTime = (System.nanoTime() - start) / 1000000
        stores.forEachIndexed { idx, s -> afterStates[idx] = s.state }

        if (!isMother) {
            for (i in beforeStates.indices) {
                val oldState = beforeStates[i]
                val newState = afterStates[i]
                if (oldState !== newState) {
                    val line = "$prelude$verticalBar ${stores[i].javaClass.name}"
                    writer.log(Log.VERBOSE, tag, "$line: $newState")
                    diffFunction?.invoke(oldState, newState)?.let { diff ->
                        writer.log(Log.VERBOSE, tag, "$line: $diff")
                    }
                }
            }
        }

        writer.log(Log.DEBUG, tag, "$prelude$downCorner$actionName ${processTime}ms")
    }
}