package mini

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