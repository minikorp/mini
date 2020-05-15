package com.minikorp.mini

/**
 * Common interface for state containers.
 */
interface StateContainer<S> {
    val state: S

    fun setState(newState: S)
}