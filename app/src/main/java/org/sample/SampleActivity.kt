package org.sample

import android.os.Bundle
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import com.mini.android.FluxActivity
import mini.*

class SampleActivity : FluxActivity() {

    private val dispatcher = MiniGen.newDispatcher()
    private val dummyStore = DummyStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        Grove.plant(ConsoleLogTree())
        val stores = listOf(dummyStore)

        MiniGen.register(dispatcher, stores)

        dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
            Grove.tag(tag).d { msg }
        }))

        dummyStore.flow()
            .collectOnUi {
                it.text
            }

        dispatcher.dispatch(ActionOne("1"))
        dispatcher.dispatch(ActionTwo("2"))
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

interface ActionInterface {
    val text: String
}

abstract class SampleAbstractAction : BaseAction()

data class ActionOne(override val text: String) : ActionInterface, SampleAbstractAction()

@Action class ActionTwo(val text: String)

data class DummyState(val text: String = "dummy")
class DummyStore : Store<DummyState>() {

    @Reducer
    fun onInterfaceAction(a: ActionInterface) {

    }

    @Reducer fun onSampleAction(a: ActionOne) {
        newState = state.copy(text = a.text)
    }

    @Reducer fun anotherAction(a: ActionTwo) {
        state.copy(text = a.text).asNewState()
    }
}