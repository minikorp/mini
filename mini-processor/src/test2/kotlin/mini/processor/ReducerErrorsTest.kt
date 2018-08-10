package mini.processor

import com.google.testing.compile.CompilationRule
import mini.Action
import mini.ReducerFun
import mini.Store
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class ReducerErrorsTest {
    private val packageName = "com.mini.pineapple"
    private val className = "PineappleApplePenActionReducer"

    @Rule
    @JvmField
    val compilation = CompilationRule()

    internal class DummyAction : Action
    internal class DummyState
    internal class DummyStateTwo

    internal class NoParamsStore : Store<DummyState>() {
        @ReducerFun
        fun helloDarknessMyOldFriend() = state
    }

    internal class ToManyParamsStore : Store<DummyState>() {
        @ReducerFun
        fun youAreNotPrepared(action: DummyAction, state: DummyState, potato: Any) = state
    }

    internal class NoReturnStore : Store<DummyState>() {
        @ReducerFun
        fun jonhWickDog(action: DummyAction, state: DummyState) {
        }
    }

    internal class NoReturnStateStore : Store<DummyState>() {
        @ReducerFun
        fun whyThanos(action: DummyAction, state: DummyState): String {
            return "I don't want to go Mr.Stark"
        }
    }

    internal class PrivateStore : Store<DummyState>() {
        @ReducerFun
        private fun whyThanos(action: DummyAction, state: DummyState) = state
    }

    internal class ProtectedStore : Store<DummyState>() {
        @ReducerFun
        protected fun whyThanos(action: DummyAction, state: DummyState) = state
    }

    internal class NotOrdererStore : Store<DummyState>() {
        @ReducerFun
        fun doYouKnowTheWae(state: DummyState, action: DummyAction) = state
    }

    internal class WrongStateStore : Store<DummyState>() {
        @ReducerFun
        fun pepe(action: DummyAction, state: DummyStateTwo) = DummyState()
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

    @Test
    fun compilation_fails_when_reducer_has_no_params() {
        assertThrows<IllegalStateException> {
            generateReducer(getElement(NoParamsStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.PARAMS_SIZE_ERROR)

        assertThrows<IllegalStateException> {
            generateReducer(getElement(ToManyParamsStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.PARAMS_SIZE_ERROR)
    }

    @Test
    fun compilation_fails_when_reducer_dont_return_state() {
        assertThrows<IllegalStateException> {
            generateReducer(getElement(NoReturnStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.RETURN_STATE_ERROR)

        assertThrows<IllegalStateException> {
            generateReducer(getElement(NoReturnStateStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.RETURN_STATE_ERROR)
    }

    @Test
    fun compilation_fails_when_reducer_not_public() {
        assertThrows<IllegalStateException> {
            generateReducer(getElement(PrivateStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.PUBLIC_FUN_ERROR)

        assertThrows<IllegalStateException> {
            generateReducer(getElement(ProtectedStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.PUBLIC_FUN_ERROR)
    }

    @Test
    fun compilation_fails_if_second_param_is_not_store_state() {
        assertThrows<IllegalStateException> {
            generateReducer(getElement(NotOrdererStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.PARAMS_ORDER_ERROR)

        assertThrows<IllegalStateException> {
            generateReducer(getElement(WrongStateStore::class.java, elements))
        }.hasMessageThat().contains(ReducerFuncModel.PARAMS_ORDER_ERROR)
    }
}