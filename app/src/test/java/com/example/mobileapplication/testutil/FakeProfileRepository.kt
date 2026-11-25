package com.example.mobileapplication.testutil

import com.example.mobileapplication.data.model.UserProfile
import com.example.mobileapplication.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * An in-memory stand-in for the DataStore profile, so ViewModel tests run on the JVM.
 *
 * @param initial The profile "already saved" when the test starts.
 */
class FakeProfileRepository(initial: UserProfile = UserProfile()) : ProfileRepository {

    private val stored = MutableStateFlow(initial)

    /** When true, [save] and [clear] throw, to test how callers handle a failure. */
    var failWrites = false

    /** What is currently saved. */
    val current: UserProfile get() = stored.value

    override val profile: Flow<UserProfile> = stored

    override suspend fun save(profile: UserProfile) {
        if (failWrites) throw IllegalStateException("Simulated DataStore failure")
        stored.value = profile
    }

    override suspend fun clear() {
        if (failWrites) throw IllegalStateException("Simulated DataStore failure")
        stored.value = UserProfile()
    }
}
