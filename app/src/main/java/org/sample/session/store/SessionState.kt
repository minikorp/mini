package org.sample.session.store

import mini.Task
import mini.taskIdle
import org.sample.session.model.User

data class SessionState(val loggedUser: User? = null,
                        val loginTask: Task = taskIdle(),
                        val logged: Boolean = false)