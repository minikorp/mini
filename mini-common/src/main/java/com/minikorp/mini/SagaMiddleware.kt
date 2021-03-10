package com.minikorp.mini


interface SagaHandler<S : Any> {
    suspend fun handle(store: Store<S>, action: Action)
}

class SagaMiddleware<S : Any>(private val sagaHandlers: List<SagaHandler<S>>) : Middleware<S> {
    override suspend fun intercept(context: DispatchContext<S>, chain: Chain<S>) {
        chain.proceed(context)
        sagaHandlers.forEach { it.handle(context.store, context.action) }
    }
}