package mini

import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

private val actionTagsCache = HashMap<KClass<*>, Set<KClass<*>>>()

/**
 * List of types this action may be observed by.
 */
val Action.reflectedTags: Set<KClass<*>>
    get() {
        return actionTagsCache.getOrPut(this::class) {
            return reflectActionTypes(this::class)
        }
    }

internal fun reflectActionTypes(type: KClass<*>): Set<KClass<*>> {
    return type.supertypes
        .asSequence()
        .map { (it.jvmErasure.java as Class<*>).kotlin }
        .map { reflectActionTypes(it) }
        .flatten()
        .plus(type)
        .toSet()
}
