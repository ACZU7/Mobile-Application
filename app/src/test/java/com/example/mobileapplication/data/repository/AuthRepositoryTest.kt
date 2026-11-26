package com.example.mobileapplication.data.repository

import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.data.model.Session
import com.example.mobileapplication.testutil.FakeAuthStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

/** Unit tests for [AuthRepository]: sign-in, lockout and expiry, guest and sign-out. */
class AuthRepositoryTest {

    private var nowMillis = 1_000_000L
    private val store = FakeAuthStore()
    private val repository = AuthRepository(store, now = { nowMillis }, hashContext = EmptyCoroutineContext)

    @Test
    fun signIn_savesTheSessionWithTheAccountsRole() = runTest {
        val result = repository.signIn("  Admin@Example.com ", "Admin-Demo-2026")

        assertEquals(SignInResult.Success(Session("admin@example.com", Role.ADMIN)), result)
        assertEquals(Session("admin@example.com", Role.ADMIN), repository.session.first())
    }

    @Test
    fun unknownEmailAndWrongPassword_giveTheSameResult() = runTest {
        assertEquals(SignInResult.InvalidCredentials, repository.signIn("nobody@example.com", "Admin-Demo-2026"))
        assertEquals(SignInResult.InvalidCredentials, repository.signIn("admin@example.com", "wrong"))
        assertNull(repository.session.first())
    }

    @Test
    fun fifthWrongAttempt_blocksForThirtySeconds_evenWithTheRightPassword() = runTest {
        repeat(4) { assertEquals(SignInResult.InvalidCredentials, repository.signIn("admin@example.com", "wrong")) }

        assertEquals(SignInResult.LockedOut(30), repository.signIn("admin@example.com", "wrong"))

        nowMillis += 10_000
        assertEquals(SignInResult.LockedOut(20), repository.signIn("admin@example.com", "Admin-Demo-2026"))
        assertNull(repository.session.first())
    }

    @Test
    fun afterTheBlockEnds_signInWorksAgain() = runTest {
        repeat(5) { repository.signIn("admin@example.com", "wrong") }

        nowMillis += AuthRepository.LOCKOUT_MILLIS

        assertEquals(SignInResult.Success(Session("admin@example.com", Role.ADMIN)), repository.signIn("admin@example.com", "Admin-Demo-2026"))
    }

    @Test
    fun success_resetsTheWrongAttemptCount() = runTest {
        repeat(4) { repository.signIn("resident@example.com", "wrong") }
        repository.signIn("resident@example.com", "Resident-Demo-2026")

        assertEquals(0, store.current.failedAttempts)
        assertEquals(SignInResult.InvalidCredentials, repository.signIn("resident@example.com", "wrong"))
    }

    @Test
    fun guestAndSignOut() = runTest {
        repository.continueAsGuest()
        assertEquals(Session(null, Role.GUEST), repository.session.first())

        repository.signOut()
        assertNull(repository.session.first())
    }
}
