package mini

class Dispatcher {
    lateinit var actionReducer: ActionReducer
    private val interceptors: MutableList<Interceptor> = ArrayList()
    private var interceptorChain: Chain = buildChain()
    private val actionReducerLink: Chain = object : Chain {
        override fun proceed(action: Action): Action {
            actionReducer.reduce(action)
            return action
        }
    }
    private var dispatching: Boolean = false

    private fun buildChain(): Chain {
        return interceptors.fold(actionReducerLink)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action = interceptor(action, chain)
            }
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
     * Post an event that will dispatch the action on the Ui thread
     * and return immediately.
     */
    fun dispatchOnUi(action: Action) {
        onUi { dispatch(action) }
    }

    /**
     * Post and event that will dispatch the action on the Ui thread
     * and block until the dispatch is complete.
     *
     * Can't be called from the main thread.
     */
    fun dispatchOnUiSync(action: Action) {
        assertNotOnUiThread()
        onUiSync { dispatch(action) }
    }

    fun dispatch(action: Action) {
        assertOnUiThread()
        if (dispatching) throw IllegalStateException("Nested dispatch calls")
        dispatching = true
        interceptorChain.proceed(action)
        dispatching = false
    }
}