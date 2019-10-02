package org.sample

import android.os.Bundle
import com.mini.android.FluxActivity
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import kotlinx.android.synthetic.main.home_activity.*
import mini.Action
import mini.LoggerInterceptor
import mini.MiniGen
import mini.Reducer
import mini.Store

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

        dispatcher.dispatch(ActionTwo("2"))
    }
}

@Action
interface ActionInterface {
    val text: String
}

@Action
class ActionTwo(override val text: String) : ActionInterface

data class DummyState(val text: String = "dummy")
class DummyStore : Store<DummyState>() {

    @Reducer
    fun hello(action: ActionInterface) {

    }
}
