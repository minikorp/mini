package mini

/**
 * Coordinator for stores mutating state. Implementation is automatically generated
 * by the compiler as MiniActionReducer.
 */
interface ActionReducer {
    /**
     * Invoke the corresponding state reducer function for the stores bound to the action.
     */
    fun reduce(action: Action)
}