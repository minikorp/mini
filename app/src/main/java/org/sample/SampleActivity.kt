package org.sample

import android.os.Bundle
import com.mini.android.FluxActivity
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import kotlinx.android.synthetic.main.home_activity.*
import mini.*

class SampleActivity : FluxActivity() {

    private val dispatcher = MiniGen.newDispatcher()
    private val dummyStore = DummyStore()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val stores = listOf(dummyStore)
        MiniGen.subscribe(dispatcher, stores).track()
        stores.forEach { it.initialize() }

        dummyStore.subscribe {
            demo_text.text = it.text
        }

        Grove.plant(ConsoleLogTree())
        dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
            Grove.tag(tag).d { msg }
        }))

        dispatcher.dispatch(ActionOne(""))
        dispatcher.dispatch(ActionTwo("2"))
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

    @Reducer fun onInterfaceAction(a: ActionInterface) {
    }

    @Reducer fun onSampleAction(a: ActionOne) {
        newState = state.copy(text = a.text)
    }

    @Reducer fun anotherAction(a: ActionTwo) {
        state.copy(text = a.text).asNewState()
    }
}