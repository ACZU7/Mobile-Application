package com.example.mobileapplication.ui.parking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.util.Consumer

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/**
 * Returns an action that asks for location permission (only if not already granted) and then reads
 * the device position once. Nothing is requested until the user taps "Find my location".
 *
 * Approximate location is accepted, because sorting nearby car parks does not need precise GPS.
 */
@Composable
fun rememberLocationRequest(
    onLocating: () -> Unit,
    onLocation: (GeoPoint) -> Unit,
    onUnavailable: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val latestOnLocating by rememberUpdatedState(onLocating)
    val latestOnLocation by rememberUpdatedState(onLocation)
    val latestOnUnavailable by rememberUpdatedState(onUnavailable)
    val latestOnDenied by rememberUpdatedState(onDenied)

    val fetch: () -> Unit = {
        latestOnLocating()
        fetchCurrentLocation(context) { point ->
            if (point != null) latestOnLocation(point) else latestOnUnavailable()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) fetch() else latestOnDenied()
    }
    return remember(context, permissionLauncher) {
        { if (hasLocationPermission(context)) fetch() else permissionLauncher.launch(LOCATION_PERMISSIONS) }
    }
}

private fun hasLocationPermission(context: Context) = LOCATION_PERMISSIONS.any {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

/**
 * Uses a cached position less than two minutes old if there is one; otherwise asks for a single fresh
 * fix, falling back to an older cached position only if the fresh request returns nothing.
 */
@SuppressLint("MissingPermission") // Only called after hasLocationPermission / the permission result.
private fun fetchCurrentLocation(context: Context, onResult: (GeoPoint?) -> Unit) {
    val manager = context.getSystemService(LocationManager::class.java) ?: return onResult(null)
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val providers = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) add(LocationManager.FUSED_PROVIDER)
        add(LocationManager.NETWORK_PROVIDER)
        if (hasFine) add(LocationManager.GPS_PROVIDER)
    }.filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

    try {
        val cached = providers.mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.elapsedRealtimeNanos }
        // A cached fix can be hours old (for example, from another town), so only trust a recent one.
        if (cached != null && cached.ageNanos() <= MAX_CACHED_AGE_NANOS) return onResult(cached.toGeoPoint())
        val provider = providers.firstOrNull() ?: return onResult(cached?.toGeoPoint())
        LocationManagerCompat.getCurrentLocation(
            manager, provider, null as CancellationSignal?, ContextCompat.getMainExecutor(context),
            Consumer { location: Location? -> onResult((location ?: cached)?.toGeoPoint()) }
        )
    } catch (e: SecurityException) {
        // Permission was revoked between the check and the request.
        onResult(null)
    }
}

private fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

private fun Location.ageNanos() = SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos

private const val MAX_CACHED_AGE_NANOS = 2L * 60 * 1_000_000_000 // two minutes
