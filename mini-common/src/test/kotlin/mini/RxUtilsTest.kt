package mini

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RxUtilsTest {

    data class TestModel(val someString: String? = null)
    data class TestState(val someTask: Task = taskIdle())
    class TestStore : Store<TestState>()

    private val testProcessor = PublishProcessor.create<TestModel>()
    private val subscriptionTracker = CompositeDisposable()
    private val testStore = TestStore()

    @Before
    fun setup() {
        subscriptionTracker.clear()
        testStore.resetState()
    }

    @Test
    fun mapNotNull_flowable_not_emit_null_values() {
        var subscribeCounter = 0
        subscriptionTracker.add(testProcessor
                .mapNotNull { it.someString }
                .subscribe {
                    subscribeCounter++
                })
        testProcessor.onNext(TestModel(null))
        assertEquals(0, subscribeCounter)
    }

    @Test
    fun mapNotNull_flowable_should_correctly_emit_values_when_they_are_not_null() {
        var subscribeCounter = 0
        subscriptionTracker.add(testProcessor
                .mapNotNull { it.someString }
                .subscribe {
                    subscribeCounter++
                })
        testProcessor.onNext(TestModel(null))
        testProcessor.onNext(TestModel("A"))
        testProcessor.onNext(TestModel("A"))
        assertEquals(2, subscribeCounter)
    }


    @Test
    fun select_flowable_should_not_emit_consecutive_same_values() {
        var subscribeCounter = 0
        subscriptionTracker.add(testProcessor
                .select { it.someString }
                .subscribe {
                    subscribeCounter++
                })
        testProcessor.onNext(TestModel(null))
        testProcessor.onNext(TestModel("A"))
        testProcessor.onNext(TestModel("A"))
        assertEquals(1, subscribeCounter)
    }


    @Test
    fun mapNotNull_observable_not_emit_null_values() {
        var subscribeCounter = 0
        subscriptionTracker.add(testProcessor
                .toObservable()
                .mapNotNull { it.someString }
                .subscribe {
                    subscribeCounter++
                })
        testProcessor.onNext(TestModel(null))
        assertEquals(0, subscribeCounter)
    }

    @Test
    fun mapNotNull_observable_should_correctly_emit_values_when_they_are_not_null() {
        var subscribeCounter = 0
        subscriptionTracker.add(testProcessor
                .toObservable()
                .mapNotNull { it.someString }
                .subscribe {
                    subscribeCounter++
                })
        testProcessor.onNext(TestModel(null))
        testProcessor.onNext(TestModel("A"))
        testProcessor.onNext(TestModel("A"))
        assertEquals(2, subscribeCounter)
    }


    @Test
    fun select_observable_should_not_emit_consecutive_same_values() {
        var subscribeCounter = 0
        subscriptionTracker.add(testProcessor
                .toObservable()
                .select { it.someString }
                .subscribe {
                    subscribeCounter++
                })
        testProcessor.onNext(TestModel(null))
        testProcessor.onNext(TestModel("A"))
        testProcessor.onNext(TestModel("A"))
        assertEquals(1, subscribeCounter)
    }

    @Test
    fun onNextTerminalState_success_is_correctly_call() {
        var called = false
        subscriptionTracker.add(testStore.flowable()
                .onNextTerminalState({ it.someTask }, { called = true }))
        testStore.setTestState(TestState(someTask = taskSuccess()))
        assertEquals(true, called)
    }

    @Test
    fun onNextTerminalState_error_is_correctly_call() {
        var called = false
        subscriptionTracker.add(testStore.flowable()
                .onNextTerminalState({ it.someTask }, failureFn = { called = true }))
        testStore.setTestState(TestState(someTask = taskFailure(Exception("Omg a task failed"))))
        assertEquals(true, called)
    }

    @Test
    fun onNextTerminalState_emits_only_once() {
        var emissionSuccessCounter = 0
        var emissionFailureCounter = 0
        subscriptionTracker.add(testStore.flowable()
                .onNextTerminalState({ it.someTask }, { emissionSuccessCounter++ }, { emissionFailureCounter++ }))
        testStore.setTestState(TestState(someTask = taskSuccess()))
        testStore.setTestState(TestState(someTask = taskFailure(Exception("Omg a task failed"))))
        assertEquals(1, emissionSuccessCounter)
        assertEquals(0, emissionFailureCounter)
    }
}