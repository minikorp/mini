package mini.processor

import java.util.*
import kotlin.reflect.KProperty

class MutableFieldProperty<R, T : Any>(private val initializer: (R) -> T) {
    private val map = WeakHashMap<R, T>()
    operator fun getValue(thisRef: R, property: KProperty<*>): T =
        map[thisRef] ?: setValue(thisRef, property, initializer(thisRef))

    operator fun setValue(thisRef: R, property: KProperty<*>, value: T): T {
        map[thisRef] = value
        return value
    }
}