package com.example.mobileapplication.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit rule that replaces Android's main thread dispatcher with a test one.
 *
 * ViewModels launch work in `viewModelScope`, which runs on `Dispatchers.Main`.
 * That dispatcher does not exist in plain JVM unit tests, so this rule swaps it
 * for [dispatcher] before each test and restores it afterwards. The default
 * unconfined dispatcher runs launched work immediately, so tests can check the
 * result straight after calling a ViewModel function.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
