package com.minikorp.mini

import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure


private fun reflectActionTypes(type: KClass<*>, depth: Int = 0): List<ReflectedType> {
    return type.supertypes
            .asSequence()
            .map { (it.jvmErasure.java as Class<*>).kotlin }
            .map { reflectActionTypes(it, depth + 1) }
            .flatten()
            .plus(ReflectedType(type, depth))
            .toList()
}

private class ReflectedType(val clazz: KClass<*>, val depth: Int)

private fun newReflectiveMap(): Map<KClass<*>, List<KClass<*>>> {
    return object : Map<KClass<*>, List<KClass<*>>> {
        private val genericTypes = listOf(Object::class)
        private val map = HashMap<KClass<*>, List<KClass<*>>>()
        override val entries: Set<Map.Entry<KClass<*>, List<KClass<*>>>> = map.entries
        override val keys: Set<KClass<*>> = map.keys
        override val size: Int = map.size
        override val values: Collection<List<KClass<*>>> = map.values
        override fun containsKey(key: KClass<*>): Boolean = map.containsKey(key)
        override fun containsValue(value: List<KClass<*>>): Boolean = map.containsValue(value)
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun get(key: KClass<*>): List<KClass<*>> {
            return map.getOrPut(key) {
                reflectActionTypes(key)
                        .asSequence()
                        .sortedBy { it.depth }
                        .map { it.clazz }
                        .filter { it !in genericTypes }
                        .toList()
            }
        }
    }
}