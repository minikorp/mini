package org.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import kotlinx.android.synthetic.main.login_activity.*
import mini.Dispatcher
import mini.onNextTerminalState
import mini.select
import org.sample.core.dagger.BaseActivity
import org.sample.session.store.LoginWithCredentialsAction
import org.sample.session.store.SessionStore
import org.sample.utils.makeGone
import org.sample.utils.makeVisible
import org.sample.utils.toast
import javax.inject.Inject

class LoginActivity : BaseActivity() {

    @Inject lateinit var dispatcher: Dispatcher
    @Inject lateinit var sessionStore: SessionStore

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        loginCredentialsButton.setOnClickListener { logInWithEmailAndPassword() }

        sessionStore.flowable()
            .select { it.loginTask }
            .subscribe {
                if (it.isRunning()) {
                    progress.makeVisible()
                } else {
                    if (it.isFailure()) {
                        toast(it.error?.message ?: "Unexpected Error")
                    }
                    progress.makeGone()
                }
            }.track()
    }

    private fun logInWithEmailAndPassword() {
        progress.makeVisible()

        val email = emailInput.text?.toString()
        val password = passwordInput.text?.toString()

        dispatcher.dispatch(LoginWithCredentialsAction(email, password))

        sessionStore.flowable()
            .onNextTerminalState(taskMapFn = { it.loginTask },
                successFn = {
                    goToHome()
                },
                failureFn = {
                    toast(it.localizedMessage)
                }).track()
    }

    private fun goToHome() {
        val intent = HomeActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}