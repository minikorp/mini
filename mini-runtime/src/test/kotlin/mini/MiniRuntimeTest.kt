package mini

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.junit.Test

class MiniRuntimeTest {

    class SampleAction : BaseAction()

    class ValidStore : Store<String>() {
        @Reducer
        fun reducer(action: SampleAction) {
        }
    }

    class InvalidStore1 : Store<String>() {
        @Reducer
        fun reducer(action: SampleAction): String {
            return "Invalid return"
        }
    }

    class InvalidStore2 : Store<String>() {
        @Reducer
        fun reducer(action: SampleAction, badParam: String): String {
            return "Invalid return"
        }
    }

    @Test
    fun types_are_reflected() {
        val map = MiniRuntime.ReflectiveActionTypesMap()
        map[SampleAction::class.java]!!.`should contain`(SampleAction::class.java)
        map[SampleAction::class.java]!!.`should contain`(BaseAction::class.java)
    }

    @Test
    fun reducers_are_found() {
        val dispatcher = Dispatcher()
        MiniRuntime.initialize(dispatcher, listOf(ValidStore()))
        dispatcher.subscriptions.size `should be equal to` 1
    }

    @Test(expected = IllegalArgumentException::class)
    fun functions_should_be_void() {
        val dispatcher = Dispatcher()
        MiniRuntime.initialize(dispatcher, listOf(InvalidStore1()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun functions_should_have_one_parameter_void() {
        val dispatcher = Dispatcher()
        MiniRuntime.initialize(dispatcher, listOf(InvalidStore2()))
    }
}