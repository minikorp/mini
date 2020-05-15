package com.minikorp.mini


class SampleStore : Store<String>() {

    companion object {
        const val INITIAL_STATE = "initial"
    }

    override fun initialState(): String = INITIAL_STATE
}