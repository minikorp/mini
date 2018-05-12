package mini

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject

class DispatcherSubscription<T : Any>(internal val dispatcher: Dispatcher,
                                      internal val id: Int,
                                      internal val priority: Int,
                                      internal val tag: Class<T>,
                                      private val cb: (T) -> Unit) : Disposable {
    private var processor: PublishProcessor<T>? = null
    private var subject: PublishSubject<T>? = null
    private var disposed = false

    override fun isDisposed(): Boolean = disposed

    internal fun onAction(action: T) {
        if (disposed) {
            Grove.e { "Subscription is disposed but got an action: $action" }
            return
        }
        cb.invoke(action)
        processor?.onNext(action)
        subject?.onNext(action)
    }

    fun flowable(): Flowable<T> {
        if (processor == null) {
            synchronized(this) {
                if (processor == null) processor = PublishProcessor.create()
            }
        }
        return processor!!
    }

    fun observable(): Observable<T> {
        if (subject == null) {
            synchronized(this) {
                if (subject == null) subject = PublishSubject.create()
            }
        }
        return subject!!
    }

    override fun dispose() {
        if (disposed) return
        synchronized(this) {
            dispatcher.unregisterInternal(this)
            disposed = true
            processor?.onComplete()
            subject?.onComplete()
        }
    }
}