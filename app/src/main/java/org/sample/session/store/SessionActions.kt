package org.sample.session.store

import mini.Action
import mini.Task
import org.sample.session.model.User

/**
 * Action dispatched to sign out the current account and reset all the data.
 */
class SignOutAction : Action

/**
 * Action dispatched to login in the app with an email and a password.
 */
data class LoginWithCredentialsAction(val email: String?, val password: String?) : Action

/**
 * Action dispatched when login process as finished.
 */
data class LoginCompleteAction(
    val user: User? = null,
    val emailVerified: Boolean = false,
    val task: Task) : Action