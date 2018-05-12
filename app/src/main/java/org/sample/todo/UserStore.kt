package org.sample.todo

import com.minivac.mini.dagger.ActivityScope
import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.dagger.ComponentFactory
import com.minivac.mini.flux.*
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.Action
import mini.Reducer
import javax.inject.Inject

@ActivityScope
@Subcomponent(modules = arrayOf(
    WarcraftModule::class,
    MightModule::class
))
interface UserComponent : StoreHolderComponent {
    fun inject(target: MainActivity)
    fun inject(target: SecondActivity)
}

object UserComponentFactory : ComponentFactory<UserComponent> {
    override fun createComponent() =
        app.findComponent(AppComponent::class)
            .mainActivityComponent()
            .also { initStores(it.stores().values) }

    override fun destroyComponent(component: UserComponent) {
        disposeStores(component.stores().values)
    }

    override val componentType = UserComponent::class
}

@Module
abstract class WarcraftModule {
    @Binds
    @ActivityScope
    @IntoMap
    @ClassKey(WarcraftStore::class)
    abstract fun storeToMap(store: WarcraftStore): Store<*>
}

@Module
abstract class MightModule {
    @Binds
    @ActivityScope
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
@ActivityScope
class WarcraftStore @Inject constructor(val dispatcher: Dispatcher) : Store<WarcraftState>() {
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

@ActivityScope
class MightStore @Inject constructor(val dispatcher: Dispatcher) : Store<StarcraftState>() {
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

