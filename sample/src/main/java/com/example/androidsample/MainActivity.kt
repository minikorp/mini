package com.example.androidsample

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.SavedStateHandle
import com.minikorp.mini.*
import com.minikorp.mini.android.FluxActivity
import com.minikorp.mini.android.FluxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.Serializable

val dispatcher = Dispatcher()

data class State(
        val text: String = "0",
        val loading: Boolean = false
) : Serializable

@Action
data class SetLoadingAction(val loading: Boolean)

@Action
data class SetTextAction(val text: String)

@Action
interface AnalyticsAction

@Action
class LongUseCaseAction(val userName: String) : AnalyticsAction, SuspendingAction


/**
 * Use any name you like for suspending actions, or use reducer
 */
typealias UseCase = Reducer


class MainStore : Store<State>() {

    init {
        Mini.link(dispatcher, this).track()
    }

    @Reducer
    fun handleLoading(state: State, action: SetLoadingAction): State {
        return state.copy(loading = action.loading)
    }


    @Reducer
    fun handleSetTextAction(state: State, action: SetTextAction): State {
        return state.copy(text = action.text)
    }

    @Reducer
    fun handleAnalyticsAction(action: AnalyticsAction) {
        //Log to analytics
    }

    @Reducer
    fun handleAnyAction(action: Any) {
        //Log to analytics
    }

    @UseCase
    suspend fun useCase(s: LongUseCaseAction) {
        if (state.loading) return
        dispatcher.dispatch(SetLoadingAction(true))
        dispatcher.dispatch(SetTextAction("Loading from network..."))
        delay(5000)
        dispatcher.dispatch(SetTextAction("Hello From UseCase"))
        dispatcher.dispatch(SetLoadingAction(false))
    }
}

class MainViewModelReducer : NestedStateContainer<State>() {

    @Reducer
    fun handleLoading(state: State, action: SetLoadingAction): State {
        return state.copy(loading = action.loading)
    }

    @Reducer
    fun handleSetTextAction(state: State, action: SetTextAction): State {
        return state.copy(text = action.text)
    }
}

class MainViewModel(savedStateHandle: SavedStateHandle) : FluxViewModel<State>(savedStateHandle) {
    private val reducerSlice = MainViewModelReducer().apply { parent = this }

    init {
        Mini.link(dispatcher, listOf(this, reducerSlice)).track()
    }

    override fun saveState(state: State, handle: SavedStateHandle) {
        println("State saved")
        handle.set("state", state)
    }

    override fun restoreState(handle: SavedStateHandle): State? {
        val restored = handle.get<State>("state")
        println("State restored $restored")
        return restored
    }

    @UseCase
    suspend fun useCase(action: LongUseCaseAction) {
        if (state.loading) return
        dispatcher.dispatch(SetLoadingAction(true))
        delay(2000)
        dispatcher.dispatch(SetTextAction("${state.text.toInt() + 1}"))
        dispatcher.dispatch(SetLoadingAction(false))
    }
}

class MainActivity : FluxActivity() {

    lateinit var textView: TextView
    lateinit var progressBar: ProgressBar
    private val vm: MainViewModel by viewModels()

    override suspend fun whenCreated(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        progressBar = findViewById(R.id.progressBar)

        textView.setOnClickListener {
            launch {
                dispatcher.dispatch(LongUseCaseAction("Pablo"))
                //Decide on the state after usecase is done
                //I won't run until use case is done
            }
        }

        vm.flow().onEach {
            textView.text = it.toString()
            progressBar.visibility = if (it.loading) View.VISIBLE else View.INVISIBLE
        }.launchInLifecycleScope()

        vm.flow()
                .select { it.loading }
                .onEachDisable {
                    Toast.makeText(this@MainActivity, "Finished loading", Toast.LENGTH_SHORT).show()
                }.launchInLifecycleScope()
    }
}
