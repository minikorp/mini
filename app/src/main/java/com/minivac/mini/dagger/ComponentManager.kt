package com.minivac.mini.dagger

import com.minivac.mini.log.Grove
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

interface ComponentManager {
    fun registerComponent(componentFactory: ComponentFactory<*>)
    fun unregisterComponent(componentFactory: ComponentFactory<*>)
    fun trimComponents(memoryLevel: Int)
    fun <T : Any> findComponent(type: KClass<T>): T
    fun <T : Any> findComponentOrNull(type: KClass<T>): T?
}

class RefCountedEntry(val component: Any,
                      val type: KClass<*>,
                      val destroyStrategy: DestroyStrategy) {
    val references: AtomicInteger = AtomicInteger(0)
}


class DefaultComponentManager : ComponentManager {

    internal val components: MutableMap<KClass<*>, RefCountedEntry> = HashMap()

    override fun registerComponent(componentFactory: ComponentFactory<*>) {
        val type = componentFactory.componentType
        components.getOrPut(type, {
            Grove.d { "Creating new component instance for: $type" }
            RefCountedEntry(
                    componentFactory.createComponent(),
                    type,
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
                .plus(componentFactory.componentType)
                .map { components[it]!! }
                .forEach {
                    val references = it.references.decrementAndGet()
                    if (references < 0) error("Unmatched calls to register / unregister")
                    if (references == 0 && it.destroyStrategy == DestroyStrategy.REF_COUNT) {
                        Grove.d { "Dropping component instance for: ${it.type}" }
                        val component = it.component
                        if (component is DisposableComponent) {
                            component.dispose()
                        }
                        components.remove(it.type)
                    }
                }
    }

    override fun trimComponents(memoryLevel: Int) {
        val toRemove = components.filterValues {
            it.references.get() == 0
                    && it.destroyStrategy.trimMemoryValue == memoryLevel
        }
        toRemove.forEach { key, value ->
            Grove.d { "Dropping component instance for: $key" }
            if (value.component is DisposableComponent) {
                value.component.dispose()
            }
            components -= key
        }
        Grove.d { "Trimmed ${toRemove.size} components" }
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> findComponent(type: KClass<T>): T {
        return findComponentOrNull(type)!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> findComponentOrNull(type: KClass<T>): T? {
        return components[type]?.component as? T
    }
}