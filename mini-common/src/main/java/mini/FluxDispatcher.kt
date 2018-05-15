package mini


interface FluxDispatcher {
    val interceptors: ArrayList<Interceptor>

    val rootChain: Chain
    var chain: Chain
    var dispatching: Boolean

    private fun buildChain(): Chain {
        return interceptors.fold(rootChain)
        { chain, interceptor ->
            object : Chain {
                override fun proceed(action: Action): Action = interceptor(action, chain)
            }
        }
    }

    fun addInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors += interceptor
            chain = buildChain()
        }
    }

    fun removeInterceptor(interceptor: Interceptor) {
        synchronized(this) {
            interceptors -= interceptor
            chain = buildChain()
        }
    }

    fun dispatch(action: Action)

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
}