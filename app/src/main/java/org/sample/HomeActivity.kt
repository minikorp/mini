package org.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.home_activity.*
import mini.Dispatcher
import mini.mapNotNull
import org.sample.core.dagger.BaseActivity
import org.sample.session.store.SessionStore
import org.sample.session.store.SignOutAction
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    @Inject
    lateinit var dispatcher: Dispatcher

    @Inject
    lateinit var sessionStore: SessionStore

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        initializeInterface()
        startListeningSessionChanges()
    }

    private fun startListeningSessionChanges() {
        sessionStore.flowable()
            .mapNotNull { it.loggedUser }
            .subscribe { updateEmail(it.email) }
            .track()

        sessionStore.flowable()
            .map { it.logged }
            .filter { !it }
            .subscribe { goToLogin() }
            .track()
    }


    private fun initializeInterface() {
        signOut.setOnClickListener { signOut() }
    }

    private fun updateEmail(value: String) {
        email.text = value
    }

    private fun signOut() {
        dispatcher.dispatch(SignOutAction())
    }

    private fun goToLogin() {
        val intent = LoginActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }
}