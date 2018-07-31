package mini.processor

import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.CompilationRule
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import mini.Reducer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class MiniProcessorTest {
    private val buffDevelopersPackage = "com.mini.buffdevs"
    private val className = "BuffDevsActionReducer"

    @Rule
    @JvmField
    val compilation = CompilationRule()

    private lateinit var elements: Elements
    private lateinit var types: Types

    @Before
    fun setUp() {
        elements = compilation.elements
        types = compilation.types
        typeUtils = types
        elementUtils = elements
    }

    private fun getElement(`class`: Class<*>): TypeElement {
        return elements.getTypeElement(`class`.canonicalName)
    }

    private fun toString(typeSpec: TypeSpec): String {
        return FileSpec.get(buffDevelopersPackage, typeSpec).toString()
    }

    private fun getTestReducer(): ActionReducerModel {
        val gymStore = getElement(GymStore::class.java)
        val bodyweightStore = getElement(BodyweightStore::class.java)
        val classElements = listOf(gymStore, bodyweightStore)
        val reducersList = classElements
                .map { it.enclosedElements }
                .flatten()
                .filter { it.isMethod && it.getAnnotation(Reducer::class.java) != null }
                .map { ReducerFuncModel(it as ExecutableElement) }

        return ActionReducerModel(reducersList)
    }

    @Test
    fun store_properties_correctly_generated() {
        val reducer = getTestReducer()
        val storeProperties = reducer.generateStoreProperties(className).build()
        assertThat(toString(storeProperties)).isEqualTo("""
            |package com.mini.buffdevs
            |
            |import mini.processor.MiniProcessorTest.DummyStore
            |
            |class TestActionReducer {
            |    val dummystore: DummyStore = stores.get(DummyStore::class.java) as DummyStore
            |}
            |""".trimMargin())
    }

    @Test
    fun main_constructor_correctly_generated() {
        val reducer = getTestReducer()
        val mainConstructor = reducer.generateMainConstructor(className).build()

        assertThat(toString(mainConstructor)).isEqualTo("""
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

        assertThat(toString(reduceMethod)).isEqualTo("""
            |package com.mini.buffdevs
            |
            |import mini.Action
            |import mini.processor.MiniProcessorTest.DummyAction
            |
            |class TestActionReducer {
            |    override fun reduce(action: Action) {
            |        action.tags.forEach { tag ->
            |            when (tag) {
            |                DummyAction::class.java -> {
            |                    action as DummyAction
            |                    dummystore.setStateInternal(dummystore.dummyReducerFunc(action))
            |                    dummystore.setStateInternal(dummystore.dummyReducerStateFunc(action))
            |                }
            |
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
        assertThat(toString(actionReducer)).isEqualTo("""
            |package com.mini.buffdevs
            |
            |import java.lang.Class
            |import kotlin.collections.Map
            |import mini.Action
            |import mini.Store
            |import mini.processor.BodyweightStore
            |import mini.processor.DeadliftAction
            |import mini.processor.GymStore
            |
            |class TestActionReducer(stores: Map<Class<*>, Store<*>>) : ActionReducer {
            |    val gymstore: GymStore = stores.get(GymStore::class.java) as GymStore
            |
            |    val bodyweightstore: BodyweightStore =
            |            stores.get(BodyweightStore::class.java) as BodyweightStore
            |
            |    override fun reduce(action: Action) {
            |        when(action) {
            |            is DeadliftAction -> {
            |                gymstore.setStateInternal(gymstore.workout(action))
            |            }
            |        }
            |    }
            |}
            |
            |""".trimMargin())
    }
}
