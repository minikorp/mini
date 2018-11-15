package org.sample.core.flux

import mini.StoreMap

/**
 * Implement this interface from any component that provides stores.
 */
interface StoreHolderComponent {
    /**
     * Returns the stored stores map.
     */
    fun stores(): StoreMap
}