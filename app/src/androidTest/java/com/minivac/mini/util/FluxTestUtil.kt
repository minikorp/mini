package com.minivac.mini.util

import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.flux.Action
import com.minivac.mini.flux.Chain
import com.minivac.mini.flux.Interceptor
import com.minivac.mini.flux.app
import mini.Grove
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.*

object TestOnlyAction : Action

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


fun cleanStateRule(): TestRule {
    return TestRule { base, description ->
        object : Statement() {

            fun reset() {
                app.findComponent(AppComponent::class)
                        .stores()
                        .values
                        .forEach {
                            it.resetState()
                        }
            }

            override fun evaluate() {
                reset()
                base.evaluate() //Execute the test
                reset()
            }
        }
    }
}

class TestDispatcherRule : TestRule {
    private val testInterceptor = TestDispatcherInterceptor()
    val actions get() = testInterceptor.actions

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val dispatcher = app
                        .findComponent(AppComponent::class)
                        .dispatcher()
                dispatcher.addInterceptor(testInterceptor)
                base.evaluate() //Execute the test
                dispatcher.removeInterceptor(testInterceptor)
            }
        }
    }

}

fun testDispatcherRule() = TestDispatcherRule()

class InitialStateRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

