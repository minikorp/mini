package com.minivac.mini.dagger

import com.minivac.mini.log.Grove
import java.util.concurrent.atomic.AtomicInteger

interface ComponentManager {
    fun registerComponent(componentFactory: ComponentFactory<*>)
    fun unregisterComponent(componentFactory: ComponentFactory<*>)
    fun trimComponents(memoryLevel: Int)
    fun <T> findComponent(name: String): T
    fun <T> findComponentOrNull(name: String): T?
}

class RefCountedEntry(val component: Any,
                      val name: String,
                      val destroyStrategy: DestroyStrategy) {
    val references: AtomicInteger = AtomicInteger(0)
}


class DefaultComponentManager : ComponentManager {

    val components: MutableMap<String, RefCountedEntry> = HashMap()

    override fun registerComponent(componentFactory: ComponentFactory<*>) {
        val name = componentFactory.componentName
        components.getOrPut(name, {
            Grove.d { "Creating new component instance for: $name" }
            RefCountedEntry(
                    componentFactory.createComponent(),
                    name,
                    componentFactory.destroyStrategy)
        }).references.incrementAndGet()

        //Add the corresponding reference, every component dependency must exist
        //otherwise the tree-like structure is broken
        componentFactory.dependencies
                .map { components[it]!! }
                .forEach { it.references.incrementAndGet() }
    }

    override fun unregisterComponent(componentFactory: ComponentFactory<*>) {
        componentFactory.dependencies
                .plus(componentFactory.componentName)
                .map { components[it]!! }
                .forEach {
                    val references = it.references.decrementAndGet()
                    if (references < 0) error("Unmatched calls to register / unregister")
                    if (references == 0 && it.destroyStrategy == DestroyStrategy.REF_COUNT) {
                        Grove.d { "Dropping component instance for: ${it.name}" }
                        val component = it.component
                        if (component is DisposableComponent) {
                            component.dispose()
                        }
                        components.remove(it.name)
                    }
                }
    }

    override fun trimComponents(memoryLevel: Int) {
        val toRemove = components.filterValues {
            it.references.get() == 0
                    && it.destroyStrategy.trimMemoryValue == memoryLevel
        }
        toRemove.forEach { key, value ->
            if (value.component is DisposableComponent) {
                value.component.dispose()
            }
            components -= key
        }
        Grove.d { "Trimmed ${toRemove.size} components" }
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T> findComponent(name: String): T {
        return findComponentOrNull<T>(name)!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> findComponentOrNull(name: String): T? {
        return components[name]?.component as? T
    }
}