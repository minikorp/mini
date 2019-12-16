package mini

/** Actions implementing this interface won't log anything */
@Action
interface SilentAction

/**
 * Action logging for stores.
 */
class LoggerMiddleware constructor(val dispatcher: Dispatcher,
                                   stores: Collection<Store<*>>,
                                   private val logFn: (tag: String, msg: String) -> Unit,
                                   private val tag: String = "MiniLog") : Middleware {

    private val stores = stores.toList()

    override suspend fun intercept(action: Any, chain: Chain): Any {
        if (action is SilentAction) chain.proceed(action) //Do nothing

        val beforeStates: Array<Any?> = Array(stores.size) { Unit }
        val afterStates: Array<Any?> = Array(stores.size) { Unit }

        stores.forEachIndexed { idx, store -> beforeStates[idx] = store.state }

        //Pass it down
        val start = System.nanoTime()
        val outAction = chain.proceed(action)
        val processTime = (System.nanoTime() - start) / 1000000

        stores.forEachIndexed { idx, store -> afterStates[idx] = store.state }

        logFn(tag, "┌────────────────────────────────────────────")
        logFn(tag, "├─> ${action.javaClass.simpleName} $processTime $action")

        for (i in beforeStates.indices) {
            val oldState = beforeStates[i]
            val newState = afterStates[i]
            if (oldState !== newState) {
                //This operation is costly, don't do it in prod
                val line = "${stores[i].javaClass.simpleName}: $newState"
                logFn(tag, "│   $line")
            }
        }

        logFn(tag, "└────────────────────────────────────────────")

        return outAction
    }
}