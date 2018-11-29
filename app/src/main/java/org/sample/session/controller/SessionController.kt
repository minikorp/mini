package org.sample.session.controller

import android.annotation.SuppressLint
import com.frangsierra.larpy.core.dagger.AppScope
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import mini.Dispatcher
import mini.taskFailure
import mini.taskSuccess
import org.sample.session.model.User
import org.sample.session.store.LoginCompleteAction
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface SessionController {
    fun loginWithCredentials(email: String?, password: String?)

    fun signOut()
}

@AppScope
class SessionControllerImpl @Inject constructor(private val dispatcher: Dispatcher) : SessionController {

    @SuppressLint("CheckResult")
    override fun loginWithCredentials(email: String?, password: String?) {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            dispatcher.dispatchAsync(
                LoginCompleteAction(
                    user = null,
                    emailVerified = false,
                    task = taskFailure(IllegalArgumentException("Email or password cant bel empty"))
                )
            )
            return
        }

        //Fake backend call
        Completable.timer(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .subscribe({
                dispatcher.dispatch(
                    LoginCompleteAction(
                        user = User("0", "Username", null, email!!),
                        emailVerified = true,
                        task = taskSuccess()
                    )
                )
            }, {
                dispatcher.dispatch(
                    LoginCompleteAction(
                        user = null,
                        emailVerified = false,
                        task = taskFailure(it)
                    )
                )
            })
    }

    override fun signOut() {
        //Backend call to sign out the user
    }
}