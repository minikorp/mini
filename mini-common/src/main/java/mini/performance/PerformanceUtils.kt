package mini.performance

import mini.log.Grove

inline fun time(crossinline block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block()
    return System.currentTimeMillis() - start
}

inline fun timeNS(crossinline block: () -> Unit): Long {
    val start = System.nanoTime()
    block()
    return System.nanoTime() - start
}

inline fun <T> timeLog(message: String, crossinline block: () -> T): T {
    val start = System.currentTimeMillis()
    val out = block()
    val elapsed = System.currentTimeMillis() - start
    Grove.d { "$message - $elapsed" }
    return out
}

inline fun <T> timeNSLog(message: String, crossinline block: () -> T): T {
    val start = System.nanoTime()
    val out = block()
    val elapsed = System.nanoTime() - start
    Grove.d { "$message - $elapsed" }
    return out
}