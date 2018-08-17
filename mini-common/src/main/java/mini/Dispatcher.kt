package mini

class Dispatcher {
    private val actionReducers: MutableList<ActionReducer> = ArrayList()
    private val interceptors: MutableList<Interceptor> = ArrayList()
    private val actionReducerLink: Chain = object : Chain {
        override fun proceed(action: Action): Action {
            actionReducers.forEach { it.reduce(action) }
            return action
        }
    }
    private var interceptorChain: Chain = buildChain()
    private var dispatching: Action? = null

    private fun buildChain(): Chain {
        return interceptors.fold(actionReducerLink)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action = interceptor(action, chain)
            }
        }
    }

    fun addActionReducer(actionReducer: ActionReducer) {
        synchronized(this) {
            actionReducers.add(actionReducer)
        }
    }

    fun removeActionReducer(actionReducer: ActionReducer) {
        synchronized(this) {
            actionReducers.remove(actionReducer)
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            interceptorChain = buildChain()
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            interceptorChain = buildChain()
        }
    }

    /**
     * Dispatch an action on the main thread synchronously.
     * This method will block the caller if it's not
     * the main thread.
     */
    fun dispatch(action: Action) {
        onUiSync {
            if (dispatching != null) {
                throw IllegalStateException("Nested dispatch calls. Currently dispatching: " +
                                            "$dispatching. Nested action: $action ")
            }
            dispatching = action
            interceptorChain.proceed(action)
            dispatching = null
        }
    }

    /**
     * Post an event that will dispatch the action on the UI thread
     * and return immediately.
     */
    fun dispatchAsync(action: Action) {
        onUi { dispatch(action) }
    }
}