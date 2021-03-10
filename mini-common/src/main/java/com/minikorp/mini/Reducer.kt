package com.minikorp.mini

import kotlin.reflect.KClass


interface Reducer<S : Any> {
    fun reduce(state: S, action: Action): S
}


@Suppress("UNCHECKED_CAST")
class DispatchContext<S : Any>(val store: Store<S>, val action: Action) {

    private val _items = HashMap<KClass<*>, Any>()
    val items: Map<KClass<*>, Any> = _items

    operator fun <T : Any> get(key: KClass<T>): T? {
        return items[key] as? T
    }

    operator fun <T : Any> set(key: KClass<T>, value: T) {
        _items[key] = value
    }
}

class IdentityReducer<S : Any> : Reducer<S> {
    override fun reduce(state: S, action: Action): S {
        return state
    }
}