package com.minivac.mini.dagger

import com.minivac.mini.log.Grove
import java.util.concurrent.atomic.AtomicInteger

interface ComponentManager {
    fun registerComponent(componentHolder: ComponentHolder<*>)
    fun unregisterComponent(componentHolder: ComponentHolder<*>)
    fun <T> findComponent(name: String): T
    fun <T> findComponentOrNull(name: String): T?
}

class RefCountedEntry(val component: Any, val name: String) {
    val references: AtomicInteger = AtomicInteger(0)
}


class DefaultComponentManager : ComponentManager {
    val components: MutableMap<String, RefCountedEntry> = HashMap()

    override fun registerComponent(componentHolder: ComponentHolder<*>) {
        val name = componentHolder.componentName
        components.getOrPut(name, {
            Grove.d { "Creating new component instance for: $name" }
            RefCountedEntry(componentHolder.createComponent(), name)
        }).references.incrementAndGet()

        //Add the corresponding reference, every component dependency must exist
        //otherwise the tree-like structure is broken
        componentHolder.dependencies
                .map { components[it]!! }
                .forEach { it.references.incrementAndGet() }
    }

    override fun unregisterComponent(componentHolder: ComponentHolder<*>) {
        componentHolder.dependencies
                .plus(componentHolder.componentName)
                .map { components[it]!! }
                .forEach {
                    val references = it.references.decrementAndGet()
                    if (references < 0) {
                        Grove.d { "Dropping component instance for: ${it.name}" }
                        components.remove(it.name)
                    }
                }
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