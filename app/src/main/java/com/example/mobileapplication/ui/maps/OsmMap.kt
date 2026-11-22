package com.example.mobileapplication.ui.maps

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mobileapplication.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import java.io.File

/**
 * A real OpenStreetMap map (osmdroid), used by the Parking and Street Issues screens.
 * No API key is needed. Tiles are downloaded over the internet and cached in this app's cache folder;
 * the "© OpenStreetMap contributors" credit required by the OSM licence is always shown.
 *
 * @param interactive false for small preview maps inside scrolling screens, so they don't steal scroll gestures.
 * @param attributionBottomPadding keeps the required OSM credit visible above a sheet that overlaps the map.
 * @param update called on every recomposition to refresh markers and the camera from the latest state.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun OsmMap(
    contentDescription: String,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    attributionBottomPadding: Dp = 0.dp,
    update: (MapView) -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val darkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val mapView = remember {
        configureOsmdroid(context)
        MapView(context).apply {
            setTileSource(OsmTiles)
            setMultiTouchControls(interactive)
            isTilesScaledToDpi = true
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            if (!interactive) setOnTouchListener { _, _ -> true }
        }
    }
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
    // clipToBounds: an embedded MapView draws tiles beyond its own edges unless the container clips it.
    Box(modifier.clipToBounds().semantics { this.contentDescription = contentDescription }) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { view ->
            // Standard OSM tiles are light; in dark mode they are turned into a neutral dark-grey map.
            view.overlayManager.tilesOverlay.setColorFilter(if (darkTheme) DarkTiles else null)
            update(view)
        }
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 4.dp, bottom = 4.dp + attributionBottomPadding)
        ) {
            Text(
                stringResource(R.string.map_attribution), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * The standard OpenStreetMap tiles. osmdroid's built-in MAPNIK source replaces the user agent with one built
 * from the package name (com.example.*), which OSM blocks, so the same server is defined here without that
 * behaviour. The policy keeps OSM's limits: no bulk downloads, no pre-fetching, at most two parallel requests.
 */
private val OsmTiles = XYTileSource(
    "OpenStreetMapStandard", 0, 19, 256, ".png", arrayOf("https://tile.openstreetmap.org/"),
    "© OpenStreetMap contributors",
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_NO_BULK or TileSourcePolicy.FLAG_NO_PREVENTIVE or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
    )
)

/**
 * Greyscale then invert: white streets become dark grey and dark labels become light, without the purple
 * tints a plain colour inversion gives to parks and roads.
 */
private val DarkTiles = ColorMatrixColorFilter(
    ColorMatrix(
        floatArrayOf(
            -0.224f, -0.440f, -0.086f, 0f, 230f,
            -0.224f, -0.440f, -0.086f, 0f, 230f,
            -0.224f, -0.440f, -0.086f, 0f, 230f,
            0f, 0f, 0f, 1f, 0f
        )
    )
)

private var osmdroidConfigured = false

/**
 * OpenStreetMap's tile usage policy (https://operations.osmfoundation.org/policies/tiles/) requires a user agent
 * that identifies the app and a way to contact its authors. Generic ids such as "com.example.*" are blocked,
 * so the app name and the project repository are sent instead. Tiles are cached (in cacheDir, which needs no
 * storage permission) so each one is downloaded only once.
 */
private const val OSM_USER_AGENT = "BristolCityCompanion/1.0 (UWE coursework prototype; +https://github.com/ACZU7/Mobile-Application)"

private fun configureOsmdroid(context: Context) {
    if (osmdroidConfigured) return
    val app = context.applicationContext
    Configuration.getInstance().apply {
        load(app, app.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        userAgentValue = OSM_USER_AGENT
        osmdroidBasePath = File(app.cacheDir, "osmdroid")
        osmdroidTileCache = File(app.cacheDir, "osmdroid/tiles")
    }
    osmdroidConfigured = true
}
