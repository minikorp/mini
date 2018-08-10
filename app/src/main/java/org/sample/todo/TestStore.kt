package org.sample.todo

import android.os.Handler
import android.os.HandlerThread
import org.sample.todo.core.dagger.AppScope
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.*
import javax.inject.Inject

data class TestState(val text: String = "Default",
                     val getTextTask: Task = taskIdle(),
                     val navigationTask: Task = taskIdle())

data class ChangeTextAction(val newText: String) : Action
class GetDataAction : Action
data class DataRetrievedAction(val task: Task,
                               val newData: String) : Action

class NavigationAction : Action
data class NavigationConditionCompleteAction(val task: Task) : Action

@AppScope
class TestStore @Inject constructor(val dispatcher: Dispatcher) : Store<TestState>() {
    val bgThread: Handler by lazy {
        val handlerThread = HandlerThread("bg")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    @Reducer
    fun changeText(action: ChangeTextAction): TestState {
        return state.copy(text = action.newText)
    }

    @Reducer
    fun getData(action: GetDataAction): TestState {
        getDataFromServer() //BG Task
        return state.copy(getTextTask = taskRunning())
    }

    @Reducer
    fun dataRetrieved(action: DataRetrievedAction): TestState {
        return state.copy(text = action.newData, getTextTask = action.task)
    }

    @Reducer
    fun startNavigationPreCondition(action: NavigationAction): TestState {
        navigationTask() //App Login for example
        return state.copy(navigationTask = taskRunning())
    }

    @Reducer
    fun NavigationConditionComplete(action: NavigationConditionCompleteAction): TestState {
        return state.copy(navigationTask = action.task)
    }

    fun getDataFromServer() {
        bgThread.post {
            Grove.i { "Download Start" }
            Thread.sleep(1000)
            Grove.i { "Download End" }
            dispatcher.dispatchOnUi(DataRetrievedAction(taskSuccess(), "I got some data from the cloud"))
        }
    }

    fun navigationTask() {
        bgThread.post {
            Grove.i { "navigation dependent task start" }
            Thread.sleep(1000)
            Grove.i { "navigation dependent task end" }
            dispatcher.dispatchOnUi(NavigationConditionCompleteAction(taskSuccess()))
        }
    }
}

@Module
abstract class TestModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(TestStore::class)
    abstract fun provideTestStore(store: TestStore): Store<*>
}