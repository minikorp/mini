package mini

/**
 * Handy alias to use with dagger
 */
typealias StoreMap = Map<Class<*>, Store<*>>

/**
 * Sort and create Stores initial state.
 */
fun initStores(uninitializedStores: Collection<Store<*>>) {
    val now = System.currentTimeMillis()

    val stores = uninitializedStores
        .toList()
        .sortedBy { it.properties[Store.INITIALIZE_ORDER_PROP] as? Int }

    val initTimes = LongArray(stores.size)
    for (i in 0 until stores.size) {
        val start = System.currentTimeMillis()
        stores[i].initialize()
        stores[i].state //Create initial state
        initTimes[i] += System.currentTimeMillis() - start
    }

    val elapsed = System.currentTimeMillis() - now

    Grove.d { "┌ Application with ${stores.size} stores loaded in $elapsed ms" }
    Grove.d { "├────────────────────────────────────────────" }
    for (i in 0 until stores.size) {
        val store = stores[i]
        var boxChar = "├"
        if (store === stores[stores.size - 1]) {
            boxChar = "└"
        }
        Grove.d { "$boxChar ${store.javaClass.simpleName} - ${initTimes[i]} ms" }
    }
}
