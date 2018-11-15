package org.sample.session.store

import mini.Dispatcher
import mini.taskFailure
import mini.taskRunning
import mini.taskSuccess
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.sample.session.controller.SessionControllerImpl
import org.sample.session.model.User

/**
 * Example of local unitary tests regarding the state changes of the stores.
 * The changes on an state must always be pure.
 * This means that the state should always been the same given an action. Making possible to test though unit tests
 */
class SessionStoreTest {

    val dispatcher = Dispatcher()
    val controller = SessionControllerImpl(dispatcher)
    val sessionStore = SessionStore(controller)

    @Before
    fun setup() {
        sessionStore.resetState()
    }

    @Test
    fun session_store_state_doesnt_change_if_login_task_is_not_running() {
        val currentState = sessionStore.state
        val error = SessionException("test error")
        val action = LoginCompleteAction(null, false, taskFailure(error))

        sessionStore.setTestState(sessionStore.onLoginComplete(action))
        Assert.assertEquals(currentState, sessionStore.state)
    }

    @Test
    fun session_store_state_changes_with_login_complete_action() {
        sessionStore.setTestState(sessionStore.state.copy(loginTask = taskRunning()))

        val error = SessionException("test error")
        val action = LoginCompleteAction(null, false, taskFailure(error))
        val expectedState = SessionState(null, taskFailure(error), false, false)

        sessionStore.setTestState(sessionStore.onLoginComplete(action))
        Assert.assertEquals(expectedState, sessionStore.state)
    }

    @Test
    fun session_store_state_goes_back_to_initial_state_on_logout() {
        val initialState = sessionStore.state

        val mockUser = User("0", "test", null, "test@gmail.com")
        sessionStore.setTestState(
            sessionStore.state.copy(
                loggedUser = mockUser,
                loginTask = taskSuccess(),
                logged = true,
                verified = true
            )
        )

        sessionStore.setTestState(sessionStore.signOut(SignOutAction()))
        Assert.assertEquals(initialState, sessionStore.state)
    }
}

class SessionException(message: String) : Exception(message)
