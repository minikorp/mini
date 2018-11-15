package org.sample

import com.agoda.kakao.KEditText
import com.agoda.kakao.KProgressBar
import com.agoda.kakao.KTextView
import com.agoda.kakao.Screen
import mini.onUiSync
import mini.taskRunning
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.sample.session.store.LoginWithCredentialsAction
import org.sample.session.store.SessionState
import org.sample.session.store.SessionStore

class LoginScreen : Screen<LoginScreen>() {
    val email: KEditText = KEditText { withId(R.id.emailInput) }
    val password: KEditText = KEditText { withId(R.id.passwordInput) }
    val credentialsLoginButton: KTextView = KTextView { withId(R.id.loginCredentialsButton) }
    val progress: KProgressBar = KProgressBar { withId(R.id.progress) }
}

class LoginActivityTest {

    @get:Rule
    val activity = testActivity(LoginActivity::class)
    @get:Rule
    val cleanState = cleanStateRule()
    @get:Rule
    val testDispatcher = testDispatcherRule()

    @Test
    fun login_with_credentials_dispatch_right_action() {
        val screen = LoginScreen()
        val anyEmail = "notADreadLord@gmail.com"
        val anyPassword = "GarroshDidNothingWrong"

        screen {
            email {
                scrollTo()
                typeText(anyEmail)
            }
            closeSoftKeyboard()

            password {
                scrollTo()
                typeText(anyPassword)
            }
            closeSoftKeyboard()

            credentialsLoginButton {
                scrollTo()
                click()
            }

            //Check action
            view.check { _, _ ->
                val expectedAction = LoginWithCredentialsAction(anyEmail, anyPassword)
                Assert.assertTrue(testDispatcher.testInterceptor.actions.any { it == expectedAction })
            }
        }
    }

    @Test
    fun progress_bar_is_show_when_logging() {
        val screen = LoginScreen()
        val sessionStore = store<SessionStore>()

        screen { progress.isGone() }

        //Set login state to success
        onUiSync {
            val state = SessionState().copy(loginTask = taskRunning())
            sessionStore.setTestState(state)
        }

        screen { progress.isVisible() }
    }
}