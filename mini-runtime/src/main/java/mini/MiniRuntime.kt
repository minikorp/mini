package mini

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

object MiniRuntime : MiniInitializer {

    override fun initialize(dispatcher: Dispatcher, stores: List<Store<*>>) {
        stores.forEach { store ->
            store::class.functions
                .map { it to it.findAnnotation<Reducer>() }
                .filter { it.second != null }
                .forEach { (fn, annotation) ->
                    if (fn.parameters.size != 2) { //param 0 is `this`
                        throw IllegalArgumentException("Function should have 1 argument for action")
                    }
                    val actionType = fn.parameters[1].type.jvmErasure
                    val priority = annotation!!.priority
                    dispatcher.register(clazz = actionType, priority = priority, callback = {
                        fn.call(store, it)
                    })
                }
        }

        dispatcher.actionTypes = ReflectiveActionTypesMap()
    }

    /**
     * Automatically extract all types, _don't use!_ this method adds ~700ms on startup!
     */
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

    class ReflectiveActionTypesMap : Map<Class<*>, List<Class<*>>> {

        private val genericTypes = listOf(
            Object::class.java
        )

        private val map = HashMap<Class<*>, List<Class<*>>>()
        override val entries: Set<Map.Entry<Class<*>, List<Class<*>>>> = map.entries
        override val keys: Set<Class<*>> = map.keys
        override val size: Int = map.size
        override val values: Collection<List<Class<*>>> = map.values
        override fun containsKey(key: Class<*>): Boolean = map.containsKey(key)
        override fun containsValue(value: List<Class<*>>): Boolean = map.containsValue(value)
        override fun isEmpty(): Boolean = map.isEmpty()
        override fun get(key: Class<*>): List<Class<*>>? {
            return map.getOrPut(key) {
                reflectActionTypes(key.kotlin)
                    .asSequence()
                    .sortedBy { it.depth }
                    .map { it.clazz.java }
                    .filter { it !in genericTypes }
                    .toList()
            }
        }
    }
}