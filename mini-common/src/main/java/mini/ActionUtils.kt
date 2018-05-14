package mini

import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure


private val actionTagsCache = HashMap<Class<*>, Set<Class<*>>>()

/**
 * List of types this action may be observed by.
 */
val Action.tags: Set<Class<*>>
    get() {
        return actionTagsCache.getOrPut(this::class.java) {
            return reflectActionTypes(this::class)
        }
    }

internal fun reflectActionTypes(type: KClass<*>): Set<Class<*>> {
    return type.supertypes
            .map { (it.jvmErasure.java as Class<*>).kotlin }
            .map { reflectActionTypes(it) }
            .flatten()
            .plus(type.java)
            .toSet()
}
