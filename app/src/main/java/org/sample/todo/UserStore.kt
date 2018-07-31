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
import mini.log.SilentAction
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
data class BecauseImHereAction(val username: String, val password: String) : Action, SilentAction

class SharedAction : Action

data class WarcraftState(val name: String = "Anonymous")
data class HeroState(val name: String = "Anonymous")

@AppScope
class WarcraftStore @Inject constructor() : Store<WarcraftState>() {

    @Reducer
    fun garrosh(action: GarroshAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }

    @Reducer
    fun illidan(action: IllidanAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }

    @Reducer
    fun durdin(action: DurdinAction, state: WarcraftState): WarcraftState {
        return state.copy(name = action.password)
    }

    //Shared
    @Reducer
    fun shared1(sharedAction: SharedAction): WarcraftState {
        return state
    }
}

@AppScope
class MightStore @Inject constructor(val dispatcher: Dispatcher) : Store<HeroState>() {

    @Reducer
    fun carolina(action: BecauseImHereAction): HeroState {
        return state.copy(name = action.username)
    }

    @Reducer(priority = 150)
    fun united(action: CarolinaSmashAction, state: HeroState): HeroState {
        return state.copy(name = action.username)
    }

    @Reducer
    fun ultra(action: PlusUltraAction, state: HeroState): HeroState {
        return state.copy(name = action.username)
    }

    //Shared
    @Reducer
    fun shared1(sharedAction: SharedAction): HeroState {
        return state
    }

    @Reducer
    fun genericReducer(sharedAction: Action): HeroState {
        return state
    }

    @Reducer
    fun genericReducer2(sharedAction: SilentAction): HeroState {
        return state
    }
}