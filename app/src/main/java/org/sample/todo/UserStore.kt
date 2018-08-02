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

interface GenericAction : Action
interface SubGenericAction : GenericAction
class ConcreteAction : SubGenericAction

@AppScope
class WarcraftStore @Inject constructor() : Store<WarcraftState>() {

    @Reducer
    fun reduceGeneric(action: GenericAction): WarcraftState {
        //This branch should appear after reduceSubGeneric
        return state
    }

    @Reducer
    fun reduceSubGeneric(action: SubGenericAction): WarcraftState {
        return state
    }

    @Reducer
    fun reduceConcrete(action: ConcreteAction): WarcraftState {
        return state
    }

    @Reducer
    fun reduceAction(action: Action): WarcraftState {
        return state
    }
}

@AppScope
class MightStore @Inject constructor(val dispatcher: Dispatcher) : Store<HeroState>() {

    @Reducer
    fun reduceConcrete(action: ConcreteAction): HeroState {
        return state
    }
}