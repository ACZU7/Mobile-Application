package com.example.mobileapplication.ui.streetissues

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.StreetCategory
import com.example.mobileapplication.ui.shared.PrimaryButton
import com.example.mobileapplication.ui.shared.SecondaryButton
import com.example.mobileapplication.ui.theme.Dimens
import com.example.mobileapplication.ui.theme.MobileApplicationTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Why the live camera cannot be shown. */
private enum class CameraBlocked { PERMISSION_DENIED, NO_CAMERA, ERROR }

/**
 * Mockup screen 5: full-screen camera with category chips, shutter, gallery and flip buttons.
 * Camera permission is requested on arrival (the user has just tapped Add photo). If it is refused,
 * the gallery and "continue without photo" remain available, so the report can always be finished.
 *
 * Always drawn with the app's dark colour scheme: a camera viewfinder is dark in both themes.
 */
@Composable
fun StreetCameraScreen(
    selectedCategory: StreetCategory?,
    onCategory: (StreetCategory) -> Unit,
    onPhoto: (String) -> Unit,
    onClose: () -> Unit
) = MobileApplicationTheme(darkTheme = true) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var blocked by rememberSaveable { mutableStateOf<CameraBlocked?>(null) }
    var hasPermission by remember { mutableStateOf(isCameraGranted(context)) }
    var asked by rememberSaveable { mutableStateOf(false) }
    var importFailed by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (!granted) blocked = CameraBlocked.PERMISSION_DENIED
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) scope.launch {
            val copy = StreetPhotoStorage.copyFromPicker(context, uri)
            if (copy != null) onPhoto(copy) else importFailed = true
        }
    }
    val openGallery = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    LaunchedEffect(Unit) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            blocked = CameraBlocked.NO_CAMERA
        } else if (!hasPermission && !asked) {
            asked = true
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(color = MaterialTheme.colorScheme.scrim, contentColor = MaterialTheme.colorScheme.onSurface, modifier = Modifier.fillMaxSize()) {
        val reason = blocked
        when {
            reason != null -> CameraUnavailable(reason, openGallery, onClose)
            hasPermission -> LiveCamera(selectedCategory, onCategory, onPhoto, onClose, openGallery) { blocked = CameraBlocked.ERROR }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }
    if (importFailed) {
        AlertDialog(
            onDismissRequest = { importFailed = false },
            title = { Text(stringResource(R.string.street_photo_import_failed_title)) },
            text = { Text(stringResource(R.string.street_photo_import_failed)) },
            confirmButton = { TextButton(onClick = { importFailed = false }) { Text(stringResource(R.string.parking_understood)) } }
        )
    }
}

@Composable
private fun LiveCamera(
    selectedCategory: StreetCategory?,
    onCategory: (StreetCategory) -> Unit,
    onPhoto: (String) -> Unit,
    onClose: () -> Unit,
    onGallery: () -> Unit,
    onCameraError: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    var lensFacing by rememberSaveable { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashOn by rememberSaveable { mutableStateOf(false) }
    var canFlip by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var captureFailed by remember { mutableStateOf(false) }

    LaunchedEffect(lensFacing) {
        val provider = cameraProvider(context)
        val wanted = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val other = CameraSelector.Builder().requireLensFacing(
            if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        ).build()
        val selector = when {
            provider.hasCamera(wanted) -> wanted
            provider.hasCamera(other) -> other
            else -> return@LaunchedEffect onCameraError()
        }
        canFlip = provider.hasCamera(wanted) && provider.hasCamera(other)
        try {
            provider.unbindAll()
            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
            val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            hasFlash = camera.cameraInfo.hasFlashUnit()
        } catch (e: IllegalArgumentException) {
            onCameraError()
        } catch (e: IllegalStateException) {
            onCameraError()
        }
    }
    DisposableEffect(Unit) {
        onDispose { ProcessCameraProvider.getInstance(context).let { future -> if (future.isDone) future.get().unbindAll() } }
    }
    imageCapture.flashMode = if (flashOn && hasFlash) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

    val previewDescription = stringResource(R.string.street_camera_preview)
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize().semantics { this.contentDescription = previewDescription })
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(Dimens.ScreenPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OverlayButton(Icons.Default.Close, stringResource(R.string.street_camera_close), onClose)
            if (hasFlash) {
                OverlayButton(
                    if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    stringResource(if (flashOn) R.string.street_camera_flash_on else R.string.street_camera_flash_off)
                ) { flashOn = !flashOn }
            }
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ScreenPadding)
        ) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Dimens.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimens.GroupSpacing)
            ) {
                // The chips use the light scheme so they match the mockup (solid coral, white cards) in both themes.
                MobileApplicationTheme(darkTheme = false) {
                    StreetCategory.entries.forEach { category ->
                        CameraCategoryChip(category, selected = category == selectedCategory) { onCategory(category) }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Dimens.ScreenPadding * 2),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                OverlayButton(Icons.Default.PhotoLibrary, stringResource(R.string.street_camera_gallery), onGallery)
                ShutterButton(enabled = !capturing) {
                    capturing = true
                    val file = StreetPhotoStorage.newPhotoFile(context)
                    imageCapture.takePicture(
                        ImageCapture.OutputFileOptions.Builder(file).build(),
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                capturing = false
                                onPhoto(Uri.fromFile(file).toString())
                            }

                            override fun onError(exception: ImageCaptureException) {
                                capturing = false
                                file.delete()
                                captureFailed = true
                            }
                        }
                    )
                }
                if (canFlip) {
                    OverlayButton(Icons.Default.Cameraswitch, stringResource(R.string.street_camera_flip)) {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                    }
                } else {
                    Spacer(Modifier.size(56.dp))
                }
            }
        }
    }
    if (captureFailed) {
        AlertDialog(
            onDismissRequest = { captureFailed = false },
            title = { Text(stringResource(R.string.street_camera_capture_failed_title)) },
            text = { Text(stringResource(R.string.street_camera_capture_failed)) },
            confirmButton = { TextButton(onClick = { captureFailed = false }) { Text(stringResource(R.string.parking_understood)) } }
        )
    }
}

/** Permission refused, no camera, or the camera failed: offer the gallery or carry on without a photo. */
@Composable
private fun CameraUnavailable(reason: CameraBlocked, onGallery: () -> Unit, onClose: () -> Unit) {
    val (title, body) = when (reason) {
        CameraBlocked.PERMISSION_DENIED -> R.string.street_camera_denied_title to R.string.street_camera_denied
        CameraBlocked.NO_CAMERA -> R.string.street_camera_none_title to R.string.street_camera_none
        CameraBlocked.ERROR -> R.string.street_camera_error_title to R.string.street_camera_error
    }
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = Dimens.MaxContentWidth).verticalScroll(rememberScrollState()).padding(Dimens.ScreenPadding * 2),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.CardSpacing)
            ) {
                Icon(Icons.Default.NoPhotography, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Text(stringResource(title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(stringResource(body), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Dimens.GroupSpacing))
                PrimaryButton(stringResource(R.string.street_camera_choose_gallery), onGallery)
                SecondaryButton(stringResource(R.string.street_camera_without_photo), onClose)
            }
        }
    }
}

@Composable
private fun OverlayButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f), modifier = Modifier.size(56.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    val description = stringResource(R.string.street_camera_take_photo)
    // White ring with a white disc inside, as in the mockup.
    Box(
        Modifier.size(76.dp).border(4.dp, MaterialTheme.colorScheme.onSurface, CircleShape).padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick, enabled = enabled, shape = CircleShape, color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxSize().semantics { this.contentDescription = description }
        ) {
            if (!enabled) Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.scrim, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            }
        }
    }
}

/** Selected chip is coral (accent) as in the mockup; others are white so they read over the viewfinder. */
@Composable
private fun CameraCategoryChip(category: StreetCategory, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    val content = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    Surface(
        onClick = onClick, shape = MaterialTheme.shapes.medium, color = container, contentColor = content,
        modifier = Modifier.size(width = 80.dp, height = 76.dp).semantics { this.selected = selected; role = Role.RadioButton }
    ) {
        Column(Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(category.icon(), contentDescription = null)
            Text(
                stringResource(category.labelRes), style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center, maxLines = 2
            )
        }
    }
}

internal fun StreetCategory.icon(): ImageVector = when (this) {
    StreetCategory.POTHOLE -> Icons.Default.Warning
    StreetCategory.STREET_LIGHT -> Icons.Default.Lightbulb
    StreetCategory.FLY_TIPPING -> Icons.Default.Delete
    StreetCategory.GRAFFITI -> Icons.Default.FormatPaint
}

private fun isCameraGranted(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private suspend fun cameraProvider(context: Context): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({ continuation.resume(future.get()) }, ContextCompat.getMainExecutor(context))
}
