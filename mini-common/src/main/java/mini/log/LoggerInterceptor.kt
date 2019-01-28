package mini.log

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import mini.Action
import mini.Chain
import mini.Interceptor
import mini.Store

/** Actions implementing this interface won't log anything */
@Action interface SilentAction

class LoggerInterceptor constructor(stores: Collection<Store<*>>,
                                    private val logInBackground: Boolean = false,
                                    private val tag: String = "MiniLog") : Interceptor {

    private val stores = stores.toList()
    private var lastActionTime = System.currentTimeMillis()
    private var actionCounter: Long = 0

    override fun invoke(action: Any, chain: Chain): Any {
        if (action is SilentAction) return chain.proceed(action) //Do nothing

        val beforeStates: Array<Any> = Array(stores.size) { Unit }
        val afterStates: Array<Any> = Array(stores.size) { Unit }

        stores.forEachIndexed { idx, store -> beforeStates[idx] = store.state }
        val start = System.currentTimeMillis()
        val timeSinceLastAction = Math.min(start - lastActionTime, 9999)
        lastActionTime = start
        actionCounter++
        val out = chain.proceed(action)
        val processTime = System.currentTimeMillis() - start
        stores.forEachIndexed { idx, store -> afterStates[idx] = store.state }

        Completable.fromAction {
            val sb = StringBuilder()
            sb.append('\n')
            sb.append("┌────────────────────────────────────────────\n")
            sb.append(String.format("├─> %s %dms [+%dms][%d] - %s",
                action.javaClass.simpleName, processTime, timeSinceLastAction, actionCounter % 10, action))
                .append("\n")

            for (i in beforeStates.indices) {
                val oldState = beforeStates[i]
                val newState = afterStates[i]
                if (oldState !== newState) {
                    //This operation is costly, don't do it in prod
                    val line = "${stores[i].javaClass.simpleName}: $newState"
                    sb.append(String.format("│   %s", line)).append("\n")
                }
            }

            sb.append("└────────────────────────────────────────────\n")
            Grove.tag(tag).d { sb.toString() }
        }.let {
            if (logInBackground) it.subscribeOn(Schedulers.single())
            else it
        }.subscribe()

        return out
    }
}