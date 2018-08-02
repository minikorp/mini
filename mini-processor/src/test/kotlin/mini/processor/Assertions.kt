package mini.processor

import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat

/**
 * Asserts if a given block of code is asserting the expected Throwable.
 */
inline fun <reified T> assertThrows(block: () -> Unit): ThrowableSubject {
    try {
        block()
    } catch (e: Throwable) {
        if (e is T) {
            return assertThat(e)
        } else {
            throw e
        }
    }
    throw AssertionError("Expected ${T::class.simpleName}")
}