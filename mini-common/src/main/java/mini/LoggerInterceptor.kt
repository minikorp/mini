package mini

import java.util.concurrent.Executors
import kotlin.math.min

/** Actions implementing this interface won't log anything */
@Action
interface SilentAction

class LoggerInterceptor constructor(stores: Collection<Store<*>>,
                                    private val logFn: (tag: String, msg: String) -> Unit,
                                    private val logInBackground: Boolean = false,
                                    private val tag: String = "MiniLog") : Interceptor {

    private val executor by lazy { Executors.newSingleThreadExecutor() }
    private val stores = stores.toList()
    private var lastActionTime = System.currentTimeMillis()
    private var actionCounter: Long = 0

    override fun invoke(action: Any, chain: Chain): Any {
        if (action is SilentAction) return chain.proceed(action) //Do nothing

        val beforeStates: Array<Any?> = Array(stores.size) { Unit }
        val afterStates: Array<Any?> = Array(stores.size) { Unit }

        stores.forEachIndexed { idx, store -> beforeStates[idx] = store.state }
        val start = System.currentTimeMillis()
        val timeSinceLastAction = min(start - lastActionTime, 9999)
        lastActionTime = start
        actionCounter++

        //Pass it down
        val out = chain.proceed(action)
        val processTime = System.currentTimeMillis() - start
        stores.forEachIndexed { idx, store -> afterStates[idx] = store.state }

        val logRunnable = Runnable {
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
            logFn(tag, sb.toString())
        }

        if (logInBackground) {
            executor.submit(logRunnable)
        } else {
            logRunnable.run()
        }
        return out
    }
}