package mini.processor

import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.CompilationRule
import mini.Action
import mini.ReducerFun
import mini.Store
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class ProcessorTest {
    private val buffDevelopersPackage = "com.mini.buffdevs"
    private val className = "BuffDevsActionReducer"

    @Rule
    @JvmField
    val compilation = CompilationRule()

    internal interface GymAction : Action
    internal data class PullUpAction(val value: Int) : Action
    internal data class RestAction(val value: Int) : Action
    internal data class DeadliftAction(val value: Int) : GymAction

    internal data class BodyweightState(val value: Int = 0)
    internal data class DeadliftState(val value: Int = 0)

    internal class BodyweightStore : Store<BodyweightState>() {
        @ReducerFun
        fun doPullUps(action: PullUpAction): BodyweightState = state.copy(value = action.value)

        @ReducerFun
        fun rest(action: RestAction, oldState: BodyweightState): BodyweightState = oldState.copy(value = action.value)
    }

    internal class GymStore : Store<DeadliftState>() {
        @ReducerFun
        fun workout(action: GymAction): DeadliftState = state.copy(value = 1)

        @ReducerFun
        fun heavyLifting(action: DeadliftAction): DeadliftState = state.copy(value = action.value)

        @ReducerFun
        fun rest(action: RestAction): DeadliftState = state.copy(value = action.value)

    }

    private lateinit var elements: Elements
    private lateinit var types: Types

    @Before
    fun setUp() {
        elements = compilation.elements
        types = compilation.types
        typeUtils = types
        elementUtils = elements
    }

    private fun getTestReducer(): ActionReducerModel {
        val gymStore = getElement(GymStore::class.java, elements)
        val bodyweightStore = getElement(BodyweightStore::class.java, elements)
        return generateReducer(gymStore, bodyweightStore)
    }

    @Test
    fun store_properties_correctly_generated() {
        val reducer = getTestReducer()
        val storeProperties = reducer.generateStoreProperties(className).build()
        assertThat(toString(storeProperties, buffDevelopersPackage)).isEqualTo("""
            |package com.mini.buffdevs
            |
            |import mini.processor.ProcessorTest
            |
            |class BuffDevsActionReducer {
            |    private val gymstore: ProcessorTest.GymStore =
            |            stores.get(ProcessorTest.GymStore::class.java) as ProcessorTest.GymStore
            |
            |    private val bodyweightstore: ProcessorTest.BodyweightStore =
            |            stores.get(ProcessorTest.BodyweightStore::class.java) as ProcessorTest.BodyweightStore
            |}
            |""".trimMargin())
    }

    @Test
    fun main_constructor_correctly_generated() {
        val reducer = getTestReducer()
        val mainConstructor = reducer.generateMainConstructor(className).build()

        assertThat(toString(mainConstructor, buffDevelopersPackage)).isEqualTo("""
        |package com.mini.buffdevs
        |
        |import java.lang.Class
        |import kotlin.collections.Map
        |import mini.Store
        |
        |class BuffDevsActionReducer(stores: Map<Class<*>, Store<*>>)
        |""".trimMargin())
    }

    @Test
    fun reduce_method_correctly_generated() {
        val reducer = getTestReducer()
        val reduceMethod = reducer.generateReduceFunc(className).build()

        assertThat(toString(reduceMethod, buffDevelopersPackage)).isEqualTo("""
            |package com.mini.buffdevs
            |
            |import mini.Action
            |import mini.processor.ProcessorTest
            |
            |class BuffDevsActionReducer {
            |    override fun reduce(action: Action) {
            |        when (action) {
            |            is ProcessorTest.DeadliftAction -> {
            |                gymstore.setStateInternal(gymstore.workout(action))
            |                gymstore.setStateInternal(gymstore.heavyLifting(action))
            |            }
            |            is ProcessorTest.GymAction -> {
            |                gymstore.setStateInternal(gymstore.workout(action))
            |            }
            |            is ProcessorTest.RestAction -> {
            |                gymstore.setStateInternal(gymstore.rest(action))
            |                bodyweightstore.setStateInternal(bodyweightstore.rest(action, bodyweightstore.state))
            |            }
            |            is ProcessorTest.PullUpAction -> {
            |                bodyweightstore.setStateInternal(bodyweightstore.doPullUps(action))
            |            }
            |        }
            |    }
            |}
            |""".trimMargin())
    }

    @Test
    fun action_reducer_correctly_generated() {
        val reducer = getTestReducer()
        val actionReducer = reducer.generateActionReducer("TestActionReducer", buffDevelopersPackage).build()
        assertThat(toString(actionReducer, buffDevelopersPackage)).isEqualTo("""
            |package com.mini.buffdevs
            |
            |import java.lang.Class
            |import kotlin.collections.Map
            |import mini.Action
            |import mini.Store
            |import mini.processor.ProcessorTest
            |
            |class TestActionReducer(stores: Map<Class<*>, Store<*>>) : ActionReducer {
            |    private val gymstore: ProcessorTest.GymStore =
            |            stores.get(ProcessorTest.GymStore::class.java) as ProcessorTest.GymStore
            |
            |    private val bodyweightstore: ProcessorTest.BodyweightStore =
            |            stores.get(ProcessorTest.BodyweightStore::class.java) as ProcessorTest.BodyweightStore
            |
            |    override fun reduce(action: Action) {
            |        when (action) {
            |            is ProcessorTest.DeadliftAction -> {
            |                gymstore.setStateInternal(gymstore.workout(action))
            |                gymstore.setStateInternal(gymstore.heavyLifting(action))
            |            }
            |            is ProcessorTest.GymAction -> {
            |                gymstore.setStateInternal(gymstore.workout(action))
            |            }
            |            is ProcessorTest.RestAction -> {
            |                gymstore.setStateInternal(gymstore.rest(action))
            |                bodyweightstore.setStateInternal(bodyweightstore.rest(action, bodyweightstore.state))
            |            }
            |            is ProcessorTest.PullUpAction -> {
            |                bodyweightstore.setStateInternal(bodyweightstore.doPullUps(action))
            |            }
            |        }
            |    }
            |}
            |""".trimMargin())
    }
}
