package com.minivac.mini.dagger

typealias ComponentKey = String

/**
 * Common interface to allow [com.minivac.mini.flux.FluxApp] component tracking,
 * used to share Dagger components between multiple activities.
 */
interface ComponentHolder<out T : Any> {

    /**
     * Factory method to create a component.
     */
    fun createComponent(): T

    /**
     * Components that should be kept active while this component is active.
     * All dependencies must exist upon registration time.
     */
    val dependencies: List<String>

    /**
     * The name of the component to add. If no such component exists with the same
     * name [createComponent] will be called.
     */
    val componentName: String
}