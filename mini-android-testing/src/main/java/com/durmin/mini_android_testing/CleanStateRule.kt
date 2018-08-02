package com.durmin.mini_android_testing

import mini.Store
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


/**
 * [TestRule] that resets the state of each Store after an evaluation.
 */
class CleanStateRule(val stores: List<Store<*>>) : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            fun reset() {
                stores.forEach { it.resetState() }
            }

            override fun evaluate() {
                reset()
                base.evaluate() //Execute the test
                reset()
            }
        }
    }
}