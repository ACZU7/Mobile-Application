package com.example.mobileapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Where the "Remind me before rent is due" choice is kept.
 * An interface so ViewModel tests can use an in-memory fake.
 */
interface RentReminderSettings {
    /** True when the user has turned the rent reminder on. */
    val reminderEnabled: Flow<Boolean>

    /** Saves the user's choice. */
    suspend fun setReminderEnabled(enabled: Boolean)
}

private val Context.rentReminderDataStore: DataStore<Preferences> by preferencesDataStore(name = "rent_reminder")

/**
 * Keeps the rent reminder choice in Preferences DataStore on the device,
 * so the switch is still on after the app is closed and opened again.
 */
class DataStoreRentReminderSettings(context: Context) : RentReminderSettings {

    private val store = context.applicationContext.rentReminderDataStore

    override val reminderEnabled: Flow<Boolean> = store.data
        // A damaged or unreadable file means "not set" rather than a crash.
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it[REMINDER_ENABLED] ?: false }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        store.edit { it[REMINDER_ENABLED] = enabled }
    }

    private companion object {
        val REMINDER_ENABLED = booleanPreferencesKey("rent_reminder_enabled")
    }
}
