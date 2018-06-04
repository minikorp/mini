package org.sample.todo

import com.minivac.mini.dagger.AppScope
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.Action
import mini.Dispatcher
import mini.Reducer
import mini.Store
import javax.inject.Inject

@Module
abstract class WarcraftModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(WarcraftStore::class)
    abstract fun storeToMap(store: WarcraftStore): Store<*>
}

@Module
abstract class MightModule {
    @Binds
    @AppScope
    @IntoMap
    @ClassKey(MightStore::class)
    abstract fun storeToMap(store: MightStore): Store<*>
}

data class GarroshAction(val username: String, val password: String) : Action
data class IllidanAction(val username: String, val password: String) : Action
data class DurdinAction(val username: String, val password: String) : Action

data class PlusUltraAction(val username: String, val password: String) : Action
data class CarolinaSmashAction(val username: String, val password: String) : Action
data class BecauseImHereAction(val username: String, val password: String) : Action

data class WarcraftState(val name: String = "Anonymous")
data class StarcraftState(val name: String = "Anonymous")

@AppScope
class WarcraftStore @Inject constructor() : Store<WarcraftState>() {

    @Reducer
    fun garroshIsOp(state : WarcraftState, action: GarroshAction): WarcraftState {
        return state.copy(name = action.password)
    }

    @Reducer
    fun illidanIsOp(action: IllidanAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }

    @Reducer
    fun durdinIsMoreOp(action: DurdinAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }

    @Reducer
    fun damnAllMight(action: BecauseImHereAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }

    @Reducer(priority = 150)
    fun fuckingDamnAllMight(action: CarolinaSmashAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }
}

@AppScope
class MightStore @Inject constructor(val dispatcher: Dispatcher) : Store<StarcraftState>() {

    @Reducer
    fun damnAllMight(state: StarcraftState, action: BecauseImHereAction): StarcraftState {
        return state.copy(name = action.username)
    }

    @Reducer(priority = 150)
    fun fuckingDamnAllMight(state: StarcraftState, action: CarolinaSmashAction): StarcraftState {
        return state.copy(name = action.username)
    }

    //TODO: Validate both params S, Action, and return type S
    @Reducer
    fun loadAllMight(state: StarcraftState, action: PlusUltraAction): StarcraftState {
        return state.copy(name = action.username)
    }
}