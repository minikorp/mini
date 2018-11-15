package org.sample.session.model


data class User(
    val uid: String,
    val username: String,
    val photoUrl: String?,
    val email: String
)