package com.minikorp.mini.test

import com.minikorp.mini.Reducer
import com.minikorp.mini.Store
import kotlinx.coroutines.yield


class ReducersStore : Store<BasicState>() {

    companion object {
        @Reducer
        fun staticImpureReducer(action: AnyAction) {

        }

        @Reducer
        suspend fun staticSuspendingImpureReducer(action: AnyAction) {
            yield()
        }

        @Reducer
        fun staticPureReducer(state: BasicState, action: AnyAction): BasicState {
            return state.copy(value = action.value)
        }

        @Reducer
        suspend fun staticSuspendingPureReducer(state: BasicState, action: AnyAction): BasicState {
            yield()
            return state.copy(value = action.value)
        }
    }

    @Reducer
    fun impureReducer(action: AnyAction) {

    }

    @Reducer
    suspend fun impureSuspendingReducer(action: AnyAction) {
        yield()
    }

    @Reducer
    fun pureReducer(state: BasicState, action: AnyAction): BasicState {
        return state.copy(value = action.value)
    }

    @Reducer
    suspend fun pureSuspendingReducer(state: BasicState, action: AnyAction): BasicState {
        yield()
        return state.copy(value = action.value)
    }
}
