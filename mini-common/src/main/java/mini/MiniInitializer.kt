package mini

/**
 * Common interface for code generation and reflection approaches.
 */
interface MiniInitializer {
    fun initialize(dispatcher: Dispatcher, stores: List<Store<*>>)
}