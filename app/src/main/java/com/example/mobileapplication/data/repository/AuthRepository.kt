package com.example.mobileapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobileapplication.data.model.Role
import com.example.mobileapplication.data.model.Session
import com.example.mobileapplication.domain.auth.DemoAccount
import com.example.mobileapplication.domain.auth.DemoAccounts
import com.example.mobileapplication.domain.auth.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.CoroutineContext

/**
 * Everything the sign-in feature remembers between launches.
 *
 * @property session Who is signed in, or null when nobody is.
 * @property failedAttempts Wrong passwords in a row since the last success or lockout.
 * @property lockedUntilMillis Sign-in is blocked until this time (epoch milliseconds); 0 when not blocked.
 */
data class AuthState(
    val session: Session? = null,
    val failedAttempts: Int = 0,
    val lockedUntilMillis: Long = 0L
)

/** Where [AuthState] is kept. An interface so tests can use an in-memory fake. */
interface AuthStore {
    val state: Flow<AuthState>

    /** Replaces the stored state with the result of [transform]. */
    suspend fun update(transform: (AuthState) -> AuthState)
}

/** The result of [AuthRepository.signIn]. */
sealed interface SignInResult {
    /** Signed in; the session is saved. */
    data class Success(val session: Session) : SignInResult

    /** The email or password is wrong. Deliberately does not say which. */
    data object InvalidCredentials : SignInResult

    /** Too many wrong attempts; sign-in is blocked for [secondsLeft] more seconds. */
    data class LockedOut(val secondsLeft: Int) : SignInResult
}

/** The part of [AuthRepository] other screens need: who is signed in, and signing out. */
interface SessionManager {
    val session: Flow<Session?>

    /** Ends the session. The demo profile and other saved data are kept. */
    suspend fun signOut()
}

/**
 * Checks the demo accounts' credentials and remembers who is signed in.
 *
 * This is a local demonstration only: there is no server and no Council account.
 * Passwords are checked against salted PBKDF2 hashes ([DemoAccounts]). After
 * [MAX_FAILED_ATTEMPTS] wrong passwords in a row, sign-in is blocked for
 * [LOCKOUT_MILLIS]; the counter and the block are saved, so closing the app
 * doesn't reset them.
 *
 * @param store Where the session and lockout state are saved.
 * @param accounts The accounts that can sign in.
 * @param now The current time in epoch milliseconds, replaceable in tests.
 * @param hashContext Where the (deliberately slow) password hashing runs. Tests pass an immediate context.
 */
class AuthRepository(
    private val store: AuthStore,
    private val accounts: List<DemoAccount> = DemoAccounts.all,
    private val now: () -> Long = System::currentTimeMillis,
    private val hashContext: CoroutineContext = Dispatchers.Default
) : SessionManager {

    override val session: Flow<Session?> = store.state.map { it.session }.distinctUntilChanged()

    /**
     * Signs in with a demo account. The email is matched without regard to case
     * or surrounding spaces.
     */
    suspend fun signIn(email: String, password: String): SignInResult {
        val before = store.state.first()
        val lockedFor = before.lockedUntilMillis - now()
        if (lockedFor > 0) return SignInResult.LockedOut(secondsLeft = ceilSeconds(lockedFor))

        val account = accounts.find { it.email == email.trim().lowercase() }
        val matches = withContext(hashContext) {
            if (account != null) {
                PasswordHasher.verify(password, account.salt, account.hash)
            } else {
                // Hash anyway so an unknown email takes as long as a wrong password.
                PasswordHasher.verify(password, UNKNOWN_ACCOUNT_SALT, ByteArray(PasswordHasher.KEY_LENGTH_BYTES))
                false
            }
        }

        if (account != null && matches) {
            val session = Session(email = account.email, role = account.role)
            store.update { AuthState(session = session) }
            return SignInResult.Success(session)
        }

        val failedAt = now()
        store.update { state ->
            val attempts = state.failedAttempts + 1
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                state.copy(failedAttempts = 0, lockedUntilMillis = failedAt + LOCKOUT_MILLIS)
            } else {
                state.copy(failedAttempts = attempts)
            }
        }
        val lockedAfter = store.state.first().lockedUntilMillis - failedAt
        return if (lockedAfter > 0) SignInResult.LockedOut(ceilSeconds(lockedAfter)) else SignInResult.InvalidCredentials
    }

    /** Saves a guest session ("Continue as guest"). */
    suspend fun continueAsGuest() {
        store.update { it.copy(session = Session.Guest) }
    }

    override suspend fun signOut() {
        store.update { it.copy(session = null) }
    }

    /** Holds the limits and the app-wide instance. */
    companion object {
        /** Wrong passwords in a row before sign-in is blocked. */
        const val MAX_FAILED_ATTEMPTS = 5

        /** How long sign-in is blocked after too many wrong passwords. */
        const val LOCKOUT_MILLIS = 30_000L

        private val UNKNOWN_ACCOUNT_SALT = ByteArray(16)

        private fun ceilSeconds(millis: Long): Int = ((millis + 999) / 1000).toInt()

        @Volatile
        private var instance: AuthRepository? = null

        /** The repository shared by the whole app, saving to DataStore. */
        fun getInstance(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(DataStoreAuthStore(context)).also { instance = it }
            }
    }
}

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session")

/**
 * Keeps the session and lockout state in Preferences DataStore
 * (`datastore/auth_session.preferences_pb`), so the user stays signed in after
 * closing the app. The file is excluded from backup and device transfer.
 */
class DataStoreAuthStore(context: Context) : AuthStore {

    private val store = context.applicationContext.authDataStore

    override val state: Flow<AuthState> = store.data
        // A damaged or unreadable file means "signed out" rather than a crash.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toAuthState() }

    override suspend fun update(transform: (AuthState) -> AuthState) {
        store.edit { prefs ->
            val next = transform(prefs.toAuthState())
            val session = next.session
            if (session == null) {
                prefs.remove(ROLE)
                prefs.remove(EMAIL)
            } else {
                prefs[ROLE] = session.role.name
                if (session.email == null) prefs.remove(EMAIL) else prefs[EMAIL] = session.email
            }
            prefs[FAILED_ATTEMPTS] = next.failedAttempts
            prefs[LOCKED_UNTIL] = next.lockedUntilMillis
        }
    }

    private fun Preferences.toAuthState(): AuthState {
        // An unknown role (for example from an older version) means "signed out".
        val role = this[ROLE]?.let { name -> Role.entries.find { it.name == name } }
        return AuthState(
            session = role?.let { Session(email = this[EMAIL], role = it) },
            failedAttempts = this[FAILED_ATTEMPTS] ?: 0,
            lockedUntilMillis = this[LOCKED_UNTIL] ?: 0L
        )
    }

    private companion object {
        val ROLE = stringPreferencesKey("role")
        val EMAIL = stringPreferencesKey("email")
        val FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        val LOCKED_UNTIL = longPreferencesKey("locked_until_millis")
    }
}
