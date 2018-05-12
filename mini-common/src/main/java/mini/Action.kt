package mini

/**
 * Common interface for all actions.
 * Tags must be types that this action implements.
 * Defaults to Any and the runtime type.
 */
interface Action {

    /**
     * List of types this action may be observed by.
     */
    val tags: Array<Class<*>>
        get() = arrayOf(Any::class.java, this.javaClass)
}