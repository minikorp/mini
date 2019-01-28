package mini

typealias Interceptor = (action: Any, chain: Chain) -> Any

/**
 * A chain of interceptors. Call [proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain {
    fun proceed(action: Any): Any
}
