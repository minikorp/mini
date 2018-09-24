package com.durmin.mini_android_testing

import mini.Action
import mini.Chain
import mini.Interceptor
import mini.log.Grove
import java.util.*

/**
 * [Interceptor] class for testing purposes which mute all the received actions.
 */
class TestDispatcherInterceptor : Interceptor {

    private val mutedActions = LinkedList<Action>()
    /** Replace all actions with dummy ones */
    override fun invoke(action: Action, chain: Chain): Action {
        Grove.d { "Muted: $action" }
        mutedActions.add(action)
        return TestOnlyAction
    }

    val actions: List<Action> get() = mutedActions
}

/**
 * Action for testing purposes.
 */
object TestOnlyAction : Action