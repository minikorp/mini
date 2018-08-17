package org.sample.todo

import android.os.Handler
import android.os.HandlerThread
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.*
import org.sample.todo.core.dagger.AppScope
import javax.inject.Inject

data class DemoState(val text: String = "Default",
                     val getTextTask: Task = taskIdle(),
                     val navigationTask: Task = taskIdle())

data class ChangeTextAction(val newText: String) : AnalyticsAction
class GetDataAction : Action
data class DataRetrievedAction(val task: Task,
                               val newData: String) : Action

class NavigationAction : Action
data class NavigationConditionCompleteAction(val task: Task) : Action

interface AnalyticsAction : Action
interface UserIdAnalyticsAction : Action
interface UserDataCrashlyticsAction : Action

class LoginCompleteAction : UserIdAnalyticsAction, AnalyticsAction, UserDataCrashlyticsAction
class CreateAccountCompleteAction : AnalyticsAction

@AppScope
class DemoStore @Inject constructor(val dispatcher: Dispatcher) : Store<DemoState>() {
    val bgThread: Handler by lazy {
        val handlerThread = HandlerThread("bg")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    @Reducer
    fun login(a: LoginCompleteAction): DemoState {
        return state
    }

    @Reducer
    fun createAccount(a: CreateAccountCompleteAction): DemoState {
        return state
    }

    @Reducer
    fun analyticsAction(analyticsAction: AnalyticsAction): DemoState {
        return state
    }

    @Reducer
    fun changeText(action: ChangeTextAction): DemoState {
        return state.copy(text = action.newText)
    }

    @Reducer
    fun getData(action: GetDataAction): DemoState {
        getDataFromServer() //BG Task
        return state.copy(getTextTask = taskRunning())
    }

    @Reducer
    fun dataRetrieved(action: DataRetrievedAction): DemoState {
        return state.copy(text = action.newData, getTextTask = action.task)
    }

    @Reducer
    fun startNavigationPreCondition(action: NavigationAction): DemoState {
        navigationTask() //App Login for example
        return state.copy(navigationTask = taskRunning())
    }

    @Reducer
    fun navigationConditionComplete(action: NavigationConditionCompleteAction): DemoState {
        return state.copy(navigationTask = action.task)
    }

    fun getDataFromServer() {
        bgThread.post {
            Grove.i { "Download Start" }
            Thread.sleep(1000)
            Grove.i { "Download End" }
            dispatcher.dispatch(DataRetrievedAction(taskSuccess(), "I got some data from the cloud"))
        }
    }

    fun navigationTask() {
        bgThread.post {
            Grove.i { "navigation dependent task start" }
            Thread.sleep(1000)
            Grove.i { "navigation dependent task end" }
            dispatcher.dispatch(NavigationConditionCompleteAction(taskSuccess()))
        }
    }
}

@Module
abstract class TestModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(DemoStore::class)
    abstract fun provideTestStore(store: DemoStore): Store<*>
}