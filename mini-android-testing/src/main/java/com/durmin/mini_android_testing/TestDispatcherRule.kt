package com.durmin.mini_android_testing

import mini.Dispatcher
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * This [TestRule] evaluates every action received with the [TestDispatcherInterceptor] to
 * intercept all the actions dispatched during a test and block them, getting them not reaching the store.
 */
class TestDispatcherRule(val dispatcher: Dispatcher) : TestRule {

    private val testInterceptor = TestDispatcherInterceptor()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                dispatcher.addInterceptor(testInterceptor)
                base.evaluate() //Execute the test
                dispatcher.removeInterceptor(testInterceptor)
            }
        }
    }

}