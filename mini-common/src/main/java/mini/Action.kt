package mini

import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

private val actionTagsCache = HashMap<Class<*>, Set<Class<*>>>()

/**
 * Common interface for all actions.
 * Tags must be types that this action implements.
 * Defaults to Any and the runtime type.
 */
interface Action {

    /**
     * List of types this action may be observed by.
     */
    val tags: Set<Class<*>>
        get() {
            return actionTagsCache.getOrPut(this::class.java) {
                return reflectActionTypes(this::class)
            }
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