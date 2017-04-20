package com.minivac.mini.dagger

import android.content.ComponentCallbacks2
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import kotlin.test.assertNotNull


@RunWith(JUnitPlatform::class)
class DefaultComponentManagerTest : Spek({

    abstract class FakeDaggerComponent : DisposableComponent

    fun createManager(): DefaultComponentManager {
        return DefaultComponentManager()
    }

    class TestComponentFactory(strategy: DestroyStrategy) : ComponentFactory<FakeDaggerComponent> {
        var disposed = false
        var created = false

        override val destroyStrategy: DestroyStrategy = strategy


        override fun createComponent(): FakeDaggerComponent {
            created = true
            return object : FakeDaggerComponent() {
                override fun dispose() {
                    disposed = true
                }
            }
        }

        override val componentType = FakeDaggerComponent::class
    }

    it("adding a component and removing it is disposes it") {
        val manager = createManager()
        val componentFactory = TestComponentFactory(DestroyStrategy.REF_COUNT)

        manager.registerComponent(componentFactory)
        assertThat(manager.components.size, equalTo(1))
        assertNotNull(manager.components[FakeDaggerComponent::class])
        assertThat(componentFactory.created, equalTo(true))

        manager.unregisterComponent(componentFactory)
        assertThat(componentFactory.disposed, equalTo(true))
    }

    it("adding a component and trimming memory disposes it"){
        val manager = createManager()
        val componentFactory = TestComponentFactory(DestroyStrategy.TRIM_MEMORY_BACKGROUND)

        manager.registerComponent(componentFactory)
        assertThat(manager.components.size, equalTo(1))
        assertNotNull(manager.components[FakeDaggerComponent::class])
        assertThat(componentFactory.created, equalTo(true))

        manager.unregisterComponent(componentFactory)
        assertThat(componentFactory.disposed, equalTo(false))

        manager.trimComponents(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND)
        assertThat(componentFactory.disposed, equalTo(true))
    }
})