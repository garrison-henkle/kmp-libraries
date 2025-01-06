package dev.henkle.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher

data class TestSuite(
    val scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    val dispatcher: TestDispatcher = StandardTestDispatcher(scheduler = scheduler),
) {
    fun runTest(
        test: suspend TestScope.() -> Unit,
    ) = kotlinx.coroutines.test.runTest(context = dispatcher) {
        test(TestScope(scope = this, scheduler = scheduler, dispatcher = dispatcher))
    }

    fun setup(block: suspend TestSuite.() -> Unit): TestSuite = apply {
        kotlinx.coroutines.test.runTest(context = dispatcher) {
            block()
        }
    }

    class TestScope(
        val scope: kotlinx.coroutines.test.TestScope,
        val scheduler: TestCoroutineScheduler,
        val dispatcher: TestDispatcher,
    ) : CoroutineScope by scope {
        fun runCurrent() = scheduler.runCurrent()
        fun advanceUntilIdle() = scheduler.advanceUntilIdle()
    }
}
