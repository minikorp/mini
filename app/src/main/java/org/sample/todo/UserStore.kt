package org.sample.todo

import com.minivac.mini.dagger.AppScope
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.Action
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
    override fun init() {

    }

    @Reducer
    public fun garroshIsOp(action: GarroshAction) {
    }

    @Reducer
    public fun illidanIsOp(action: IllidanAction) {
    }

    @Reducer
    public fun durdinIsMoreOp(action: DurdinAction) {
    }

    @Reducer
    public fun damnAllMight(action: BecauseImHereAction) {
    }

    @Reducer
    public fun fuckingDamnAllMight(action: CarolinaSmashAction) {
    }
}

@AppScope
class MightStore @Inject constructor() : Store<StarcraftState>() {
    override fun init() {

    }

    @Reducer
    public fun damnAllMight(action: BecauseImHereAction) {
    }

    @Reducer
    public fun fuckingDamnAllMight(action: CarolinaSmashAction) {
    }

    @Reducer
    public fun kawaiNoDesuNeAllMight(action: PlusUltraAction) {
    }
}

