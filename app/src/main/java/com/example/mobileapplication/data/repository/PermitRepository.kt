package com.example.mobileapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.ParkingZone
import com.example.mobileapplication.data.model.ResidentPermit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException
import java.time.LocalDate

/** The fixed demonstration permit and zone. Every value is fictional. */
object SamplePermit {
    val zone = ParkingZone("CM", R.string.permit_zone_hours, R.string.permit_zone_rules)

    val permit = ResidentPermit(
        zoneCode = zone.code,
        reference = "RP-CM-20417",
        vehicleRegistration = "SA25 MPL",
        expiryDate = LocalDate.of(2027, 3, 31)
    )
}

/** Where the resident's permit and the reminder choice are kept. An interface so tests can use a fake. */
interface PermitStore {
    val permit: Flow<ResidentPermit>
    val reminderEnabled: Flow<Boolean>
    suspend fun setReminderEnabled(enabled: Boolean)
}

private val Context.permitDataStore: DataStore<Preferences> by preferencesDataStore(name = "resident_permit")

/**
 * Keeps the sample permit and reminder choice in Preferences DataStore, so both survive an app restart.
 * The sample permit is written on first use; nothing is sent off the device.
 */
class DataStorePermitStore(context: Context) : PermitStore {
    private val store = context.applicationContext.permitDataStore

    private val preferences: Flow<Preferences> = store.data
        .onStart { seedSamplePermitIfMissing() }
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }

    override val permit: Flow<ResidentPermit> = preferences.map { it.toPermit() ?: SamplePermit.permit }

    override val reminderEnabled: Flow<Boolean> = preferences.map { it[REMINDER_ENABLED] ?: false }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        store.edit { it[REMINDER_ENABLED] = enabled }
    }

    private suspend fun seedSamplePermitIfMissing() {
        store.edit { prefs ->
            if (prefs[ZONE] == null) {
                val sample = SamplePermit.permit
                prefs[ZONE] = sample.zoneCode
                prefs[REFERENCE] = sample.reference
                prefs[VEHICLE] = sample.vehicleRegistration
                prefs[EXPIRY] = sample.expiryDate.toString()
            }
        }
    }

    private fun Preferences.toPermit(): ResidentPermit? {
        val zone = this[ZONE] ?: return null
        val expiry = this[EXPIRY]?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        return ResidentPermit(zone, this[REFERENCE].orEmpty(), this[VEHICLE].orEmpty(), expiry)
    }

    private companion object {
        val ZONE = stringPreferencesKey("zone_code")
        val REFERENCE = stringPreferencesKey("reference")
        val VEHICLE = stringPreferencesKey("vehicle_registration")
        val EXPIRY = stringPreferencesKey("expiry_date")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    }
}
