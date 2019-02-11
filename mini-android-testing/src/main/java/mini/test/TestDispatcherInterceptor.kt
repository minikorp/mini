package mini.test

import mini.BaseAction
import mini.Chain
import mini.Interceptor
import java.util.*

/**
 * [Interceptor] class for testing purposes which mute all the received actions.
 */
class TestDispatcherInterceptor : Interceptor {

    private val mutedActions = LinkedList<Any>()
    /** Replace all actions with dummy ones */
    override fun invoke(action: Any, chain: Chain): Any {
        mutedActions.add(action)
        return TestOnlyAction
    }

    val actions: List<Any> get() = mutedActions
}

/**
 * Action for testing purposes.
 */
object TestOnlyAction : BaseAction()