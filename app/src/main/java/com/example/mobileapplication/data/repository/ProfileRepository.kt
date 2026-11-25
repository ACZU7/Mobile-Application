package com.example.mobileapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobileapplication.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Where the local demonstration [UserProfile] is kept.
 * An interface so ViewModel tests can use an in-memory fake.
 */
interface ProfileRepository {

    /** The saved profile. Emits an empty [UserProfile] when nothing has been saved. */
    val profile: Flow<UserProfile>

    /** Replaces the saved profile. Null fields are removed. */
    suspend fun save(profile: UserProfile)

    /** Deletes the whole profile from the phone. */
    suspend fun clear()
}

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "demo_profile")

/**
 * Keeps the demo profile in Preferences DataStore, so it is still there after
 * the app is closed and opened again. It is never sent anywhere.
 */
class DataStoreProfileRepository(context: Context) : ProfileRepository {

    private val store = context.applicationContext.profileDataStore

    override val profile: Flow<UserProfile> = store.data
        // A damaged or unreadable file means "no profile" rather than a crash.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { prefs ->
            UserProfile(
                name = prefs[NAME],
                email = prefs[EMAIL],
                postcode = prefs[POSTCODE],
                vehicleRegistration = prefs[VEHICLE]
            )
        }
        .distinctUntilChanged()

    override suspend fun save(profile: UserProfile) {
        store.edit { prefs ->
            prefs.setOrRemove(NAME, profile.name)
            prefs.setOrRemove(EMAIL, profile.email)
            prefs.setOrRemove(POSTCODE, profile.postcode)
            prefs.setOrRemove(VEHICLE, profile.vehicleRegistration)
        }
    }

    override suspend fun clear() {
        store.edit { it.clear() }
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.setOrRemove(
        key: Preferences.Key<String>,
        value: String?
    ) {
        if (value == null) remove(key) else this[key] = value
    }

    private companion object {
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val POSTCODE = stringPreferencesKey("postcode")
        val VEHICLE = stringPreferencesKey("vehicle_registration")
    }
}
