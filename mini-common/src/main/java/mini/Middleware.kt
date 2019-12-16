package mini

interface Middleware {
    suspend fun intercept(action: Any, chain: Chain): Any
}

/**
 * A chain of interceptors. Call [proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain {
    suspend fun proceed(action: Any): Any
}
