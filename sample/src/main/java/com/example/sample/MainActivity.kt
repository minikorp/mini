package com.example.sample

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sample.another.RootState
import com.example.sample.another.RootStateReducer
import com.minikorp.mini.Action
import com.minikorp.mini.LoggerMiddleware
import com.minikorp.mini.SagaHandler
import com.minikorp.mini.MotherAction
import com.minikorp.mini.IdentityReducer
import com.minikorp.mini.Reducer
import com.minikorp.mini.SagaMiddleware
import com.minikorp.mini.State
import com.minikorp.mini.Store
import com.minikorp.mini.TypedAction
import com.minikorp.mini.TypedReducer
import com.minikorp.mini.TypedReducerRoot
import com.minikorp.mini.flow
import com.minikorp.mini.onEachDisable
import com.minikorp.mini.select
import java.io.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@State
data class MainState(
        val text: String = "0",
        val loading: Boolean = false
) : Serializable


@State
data class SessionState(
        val text: String = "0",
        val loading: Boolean = false
) : Serializable


@State
data class WorkoutState(
        val text: String = "0",
        val loading: Boolean = false,
        val nested: SessionState = SessionState()
) : Serializable

@TypedAction
data class SetLoadingAction(val loading: Boolean) : Action

@TypedAction
data class SetTextAction(val text: String) : Action

@TypedAction
interface AnalyticsAction : Action

@TypedAction
class LongUseCaseAction : Action, AnalyticsAction, MotherAction

class MainReducer : Reducer<MainState> {

    @TypedReducer
    fun setLoading(state: MainState, action: SetLoadingAction): MainState {
        return state.copy(loading = action.loading)
    }

    @TypedReducer
    fun setText(state: MainState, action: SetTextAction): MainState {
        return state.copy(text = action.text)
    }

    @TypedReducerRoot
    override fun reduce(state: MainState, action: Action): MainState {
        return reduceTyped(state, action) ?: state
    }
}

class MainSagaHandler : SagaHandler<RootState> {

    @TypedReducer
    suspend fun handleLongUseCase(store: Store<RootState>, action: LongUseCaseAction) {
        if (store.state.main.loading) return
        store.dispatch(SetLoadingAction(true))
        delay(2000)
        store.dispatch(SetTextAction("${store.state.main.text.toInt() + 1}"))
        store.dispatch(SetLoadingAction(false))
    }

    @TypedReducerRoot
    override suspend fun handle(store: Store<RootState>, action: Action) {
        handleTyped(store, action)
    }
}

val store: Store<RootState> = Store(
        initialState = RootState(),
        storeScope = CoroutineScope(Dispatchers.Main.immediate),
        reducer = RootStateReducer(
                mainReducer = MainReducer(),
                workoutReducer = WorkoutStateReducer(
                        nestedReducer = IdentityReducer()
                ),
                sessionReducer = IdentityReducer(),
        )
).apply {
    addMiddleware(LoggerMiddleware())
    addMiddleware(SagaMiddleware(listOf(MainSagaHandler())))
}

class MainActivity : AppCompatActivity() {

    lateinit var textView: TextView
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        progressBar = findViewById(R.id.progressBar)

        textView.setOnClickListener {
            lifecycleScope.launch {
                store.dispatch(LongUseCaseAction())
                //Decide on the state after usecase is done
                //I won't run until use case is done
                Toast.makeText(this@MainActivity, "Finished loading", Toast.LENGTH_SHORT).show()
            }
        }

        store.flow()
                .select { it.main }
                .onEach {
                    textView.text = it.toString()
                    progressBar.visibility = if (it.loading) View.VISIBLE else View.INVISIBLE
                }.launchIn(lifecycleScope)

        store.flow()
                .select { it.main.loading }
                .onEachDisable {
                    Toast.makeText(this@MainActivity, "Finished loading", Toast.LENGTH_SHORT).show()
                }.launchIn(lifecycleScope)
    }
}
