package org.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import kotlinx.android.synthetic.main.home_activity.*
import mini.*

class SampleActivity : AppCompatActivity(), SubscriptionTracker by DefaultSubscriptionTracker() {

    private val dispatcher = Dispatcher(Mini.actionTypes)
    private val dummyStore = DummyStore(dispatcher)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        Grove.plant(ConsoleLogTree())
        val stores = listOf(dummyStore)

        Mini.register(dispatcher, stores)
        dispatcher.addInterceptor(LoggerInterceptor(stores, { tag, msg ->
            Grove.tag(tag).d { msg }
        }))

        dummyStore.flowable().subscribe {
            email.text = it.text
        }.track()

        dispatcher.dispatch(ActionOne("1"))
        dispatcher.dispatch(ActionTwo("2"))
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelSubscriptions()
    }
}

interface ActionInterface {
    val text: String
}

abstract class SampleAbstractAction : BaseAction()

data class ActionOne(override val text: String) : ActionInterface, SampleAbstractAction()

@Action class ActionTwo(val text: String)

data class DummyState(val text: String = "dummy")
class DummyStore(dispatcher: Dispatcher) : Store<DummyState>(dispatcher) {

    @Reducer fun onInterfaceAction(a: ActionInterface) {

    }

    @Reducer fun onSampleAction(a: ActionOne) {
        setState(state.copy(text = a.text))
    }

    @Reducer fun anotherAction(a: ActionTwo) {
        setState(state.copy(text = a.text))
    }
}