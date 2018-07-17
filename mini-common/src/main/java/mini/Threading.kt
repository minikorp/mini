package mini

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Semaphore

val uiHandler by lazy { Handler(Looper.getMainLooper()) }

fun isOnUi(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
}

fun assertNotOnUiThread() {
    if (!isOnUi()) {
        error("This method can not be called from the main application thread")
    }
}

fun assertOnUiThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        error("This method can only be called from the main application thread")
    }
}

inline fun onUi(delayMs: Long = 0, crossinline block: () -> Unit) {
    if (delayMs > 0) uiHandler.postDelayed({ block() }, delayMs)
    else uiHandler.post { block() }
}

inline fun onUiSync(crossinline block: () -> Unit) {
    if (isOnUi()) {
        block()
    } else {
        val sem = Semaphore(0)
        onUi {
            block()
            sem.release()
        }
        sem.acquireUninterruptibly()
    }
}