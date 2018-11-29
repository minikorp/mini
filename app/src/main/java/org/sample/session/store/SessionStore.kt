package org.sample.session.store

import com.frangsierra.larpy.core.dagger.AppScope
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.Reducer
import mini.Store
import mini.taskRunning
import org.sample.session.controller.SessionController
import org.sample.session.controller.SessionControllerImpl
import javax.inject.Inject

@AppScope
class SessionStore
@Inject constructor(private val controller: SessionController) : Store<SessionState>() {

    @Reducer
    fun loginWithCredentials(a: LoginWithCredentialsAction): SessionState {
        if (state.loginTask.isRunning()) return state
        controller.loginWithCredentials(a.email, a.password)
        return state.copy(loginTask = taskRunning())
    }

    @Reducer
    fun onLoginComplete(a: LoginCompleteAction): SessionState {
        if (!state.loginTask.isRunning()) return state
        return state.copy(
            loggedUser = a.user,
            loginTask = a.task,
            logged = a.user != null
        )
    }

    @Reducer
    fun onUpdateEmailAction(a: UpdateEmailAction): SessionState {
        return state.copy(
            loggedUser = state.loggedUser?.copy(
                email = a.newEmail
            )
        )
    }

    @Reducer
    fun onSignOut(a: SignOutAction): SessionState {
        controller.signOut()
        return initialState()
    }
}

@Module
abstract class SessionModule {
    @Binds @AppScope @IntoMap @ClassKey(SessionStore::class)
    abstract fun provideSessionStore(store: SessionStore): Store<*>

    @Binds @AppScope
    abstract fun bindSessionController(impl: SessionControllerImpl): SessionController
}