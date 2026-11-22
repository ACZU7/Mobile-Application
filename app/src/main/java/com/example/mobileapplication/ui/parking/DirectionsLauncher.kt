package com.example.mobileapplication.ui.parking

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import java.util.Locale

/**
 * Hands a car park's coordinates to a maps app.
 *
 * It is an interface so the Parking screens can be tested with a fake that pretends
 * no maps app or browser is installed.
 */
fun interface DirectionsLauncher {

    /** Tries to open directions to the coordinates. Returns false if nothing on the phone can show them. */
    fun open(latitude: Double, longitude: Double, label: String): Boolean
}

/**
 * The real [DirectionsLauncher]. Tries Google Maps first, then any other maps app,
 * then a browser. Each step is tried in turn because none of them is guaranteed to be installed.
 */
class MapsDirectionsLauncher(private val context: Context) : DirectionsLauncher {

    override fun open(latitude: Double, longitude: Double, label: String): Boolean {
        val directionsUri = DirectionsUris.googleMapsDirections(latitude, longitude).toUri()
        val candidates = listOf(
            Intent(Intent.ACTION_VIEW, directionsUri).setPackage(GOOGLE_MAPS_PACKAGE),
            Intent(Intent.ACTION_VIEW, DirectionsUris.geo(latitude, longitude, Uri.encode(label)).toUri()),
            Intent(Intent.ACTION_VIEW, directionsUri)
        )
        return candidates.any(::tryStart)
    }

    private fun tryStart(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    private companion object {
        const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    }
}

/** Builds the addresses passed to maps apps. Kept free of Android types so it can be unit tested. */
object DirectionsUris {

    /** Google Maps directions URL; opens in the Maps app when installed, otherwise in a browser. */
    fun googleMapsDirections(latitude: Double, longitude: Double): String =
        "https://www.google.com/maps/dir/?api=1&destination=${coordinates(latitude, longitude)}"

    /** Generic `geo:` address understood by most Android maps apps. [encodedLabel] must already be URI-encoded. */
    fun geo(latitude: Double, longitude: Double, encodedLabel: String): String {
        val point = coordinates(latitude, longitude)
        return "geo:$point?q=$point($encodedLabel)"
    }

    // Locale.US keeps a '.' decimal separator, even when the phone uses Arabic or Polish.
    private fun coordinates(latitude: Double, longitude: Double) =
        String.format(Locale.US, "%.6f,%.6f", latitude, longitude)
}
