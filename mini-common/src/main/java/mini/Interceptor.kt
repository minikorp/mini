package mini

typealias Interceptor = (action: Action, chain: Chain) -> Action

/**
 * A chain of interceptors. Call [.proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain {
    fun proceed(action: Action): Action
}
