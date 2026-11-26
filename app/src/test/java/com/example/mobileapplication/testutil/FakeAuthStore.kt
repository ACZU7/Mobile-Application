package com.example.mobileapplication.testutil

import com.example.mobileapplication.data.repository.AuthState
import com.example.mobileapplication.data.repository.AuthStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** An in-memory stand-in for the DataStore session, so auth tests run on the JVM. */
class FakeAuthStore(initial: AuthState = AuthState()) : AuthStore {
    private val stored = MutableStateFlow(initial)

    /** What is currently saved. */
    val current: AuthState get() = stored.value

    override val state: Flow<AuthState> = stored

    override suspend fun update(transform: (AuthState) -> AuthState) {
        stored.value = transform(stored.value)
    }
}
