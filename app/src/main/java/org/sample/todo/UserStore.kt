package org.sample.todo

import mini.Action
import mini.Reducer
import com.minivac.mini.dagger.ActivityScope
import com.minivac.mini.dagger.AppComponent
import com.minivac.mini.dagger.ComponentFactory
import com.minivac.mini.flux.*
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import javax.inject.Inject


@ActivityScope
@Subcomponent(modules = arrayOf(
        UserModule::class
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
abstract class UserModule {
    @Binds @ActivityScope @IntoMap @ClassKey(UserStore::class)
    abstract fun storeToMap(store: UserStore): Store<*>
}

data class LoginUserAction(val username: String, val password: String) : Action

data class UserState(val name: String = "Anonymous")
@ActivityScope
class UserStore @Inject constructor(val dispatcher: Dispatcher) : Store<UserState>() {
    override fun init() {

    }

    @Reducer
    public fun loginUser(loginUserAction: LoginUserAction){

    }
}
