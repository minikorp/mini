package org.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.mini.android.FluxActivity
import com.minikorp.grove.ConsoleLogTree
import com.minikorp.grove.Grove
import kotlinx.android.synthetic.main.home_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mini.Action
import mini.BaseSaga
import mini.Dispatcher
import mini.LoggerMiddleware
import mini.MiniGen
import mini.ObjectDiff
import mini.Reducer
import mini.Saga
import mini.Store

class SampleActivity : FluxActivity() {

    override val dispatcher = Dispatcher(MiniGen.actionTypes)
    private val dummyStore = DummyStore(dispatcher)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        val stores = listOf(dummyStore)
        MiniGen.subscribe(dispatcher, stores).track()
        stores.forEach { it.initialize() }

        dummyStore.subscribe {
            demo_text.text = "${it.loginState} - ${it.user}"
        }.track()



        Grove.plant(ConsoleLogTree())
        dispatcher.addMiddleware(LoggerMiddleware(stores,
            diffFunction = { a, b -> ObjectDiff.computeDiff(a, b) },
            logger = { p, tag, msg ->
                Grove.tag(tag).log(p) { msg }
            }))


        //Perform login

        container.setOnClickListener {
            val job = lifecycleScope.launch {
                dispatcher.dispatch(LoginAction())
                Grove.d { "Login complete!" }
            }
        }
    }
}


@Action
interface ActionInterface {
    val text: String
}

@Action
class LoginAction : BaseSaga()

@Action
class LoginStartAction

@Action
data class LoginCompleteAction(val state: String)

data class DummyState(
    val text: String = "dummy",
    val user: String = "Anon",
    val loginState: String = "idle"
)

class DummyStore(private val dispatcher: Dispatcher) : Store<DummyState>() {

    @Saga suspend fun onLogin(action: LoginAction) {
        dispatcher.dispatch(LoginStartAction())
        withContext(Dispatchers.IO + SupervisorJob()) {
            try {
                delay(5000) //Login for 5 seconds
                dispatcher.dispatch(LoginCompleteAction("success"))
            } catch (ex: Throwable) {
                withContext(NonCancellable) {
                    //Job was cancelled or failed, so we can't dispatch on the same context, start new one
                    dispatcher.dispatch(LoginCompleteAction("failure"))
                }
            }
        }
    }

    @Reducer fun onLoginStarted(action: LoginStartAction) {
        newState = state.copy(loginState = "running")
    }

    @Reducer fun onLoginComplete(action: LoginCompleteAction) {
        newState = state.copy(loginState = action.state, user = "Mini")
    }
}
