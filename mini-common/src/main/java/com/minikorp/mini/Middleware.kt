package com.minikorp.mini

/**
 * Middleware that will be called for every dispatch to modify the
 * action or perform side effects like logging.
 *
 * Call chain.proceed(action) with the new action or dispatcher chain will be broken.
 */
interface Middleware<S : Any> {
    suspend fun intercept(context: DispatchContext<S>, chain: Chain<S>)
}

/**
 * A chain of interceptors. Call [proceed] with
 * the intercepted action or directly handle it.
 */
interface Chain<S : Any> {
    suspend fun proceed(context: DispatchContext<S>)
}