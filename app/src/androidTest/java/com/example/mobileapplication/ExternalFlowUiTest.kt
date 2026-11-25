package com.example.mobileapplication

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.mobileapplication.ui.parking.MapsDirectionsLauncher
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ExternalFlowUiTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private fun screenshot(name: String) {
        val image = instrumentation.uiAutomation.takeScreenshot()
        val dir = File(instrumentation.targetContext.filesDir, "final-evidence").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }
    private fun nodes(node: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> =
        if (node == null) emptyList() else listOf(node) + (0 until node.childCount).flatMap { nodes(node.getChild(it)) }

    @Test fun systemGalleryPhotoIsCopiedAndCanBeRemoved() {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("externalFlows") == "true")
        val context = instrumentation.targetContext
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Bristol-gallery-test.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BristolTest")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
        resolver.openOutputStream(uri)!!.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }; bitmap.recycle()
        try {
            ui.onNodeWithText("Streets").performClick()
            ui.onNodeWithText("Add photo").performScrollTo().performClick()
            ui.waitUntil(10000) { ui.onAllNodesWithContentDescription("Choose from gallery").fetchSemanticsNodes().isNotEmpty() ||
                ui.onAllNodesWithText("Choose from gallery").fetchSemanticsNodes().isNotEmpty() }
            val icon = ui.onAllNodesWithContentDescription("Choose from gallery")
            if (icon.fetchSemanticsNodes().isNotEmpty()) icon.onFirst().performClick()
            else ui.onNodeWithText("Choose from gallery").performClick()
            ui.waitUntil(15000) { nodes(instrumentation.uiAutomation.rootInActiveWindow).any {
                it.viewIdResourceName?.contains("icon_thumbnail") == true ||
                    it.contentDescription?.toString()?.contains("Photo taken") == true } }
            screenshot("08-system-gallery")
            val target = nodes(instrumentation.uiAutomation.rootInActiveWindow).first {
                it.viewIdResourceName?.contains("icon_thumbnail") == true || it.contentDescription?.toString()?.contains("Photo taken") == true }
            val bounds = android.graphics.Rect().also { target.getBoundsInScreen(it) }
            val time = android.os.SystemClock.uptimeMillis()
            listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP).forEach { action ->
                val event = android.view.MotionEvent.obtain(time, android.os.SystemClock.uptimeMillis(), action,
                    bounds.exactCenterX(), bounds.exactCenterY(), 0).apply { source = android.view.InputDevice.SOURCE_TOUCHSCREEN }
                instrumentation.uiAutomation.injectInputEvent(event, true); event.recycle()
            }
            // Newer system pickers retain a selected photo until Done is pressed.
            fun confirm(node: AccessibilityNodeInfo) = node.text?.toString()?.let {
                it == "Done" || it == "Add" || it.startsWith("Add (")
            } == true
            ui.waitUntil(10000) { nodes(instrumentation.uiAutomation.rootInActiveWindow).any(::confirm) ||
                runCatching { ui.onAllNodesWithContentDescription("Remove photo 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
            nodes(instrumentation.uiAutomation.rootInActiveWindow).firstOrNull(::confirm)?.let { done ->
                val rect = android.graphics.Rect().also { done.getBoundsInScreen(it) }
                val started = android.os.SystemClock.uptimeMillis()
                listOf(android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_UP).forEach { action ->
                    val event = android.view.MotionEvent.obtain(started, android.os.SystemClock.uptimeMillis(), action,
                        rect.exactCenterX(), rect.exactCenterY(), 0).apply { source = android.view.InputDevice.SOURCE_TOUCHSCREEN }
                    instrumentation.uiAutomation.injectInputEvent(event, true); event.recycle()
                }
            }
            try { ui.waitUntil(15000) { runCatching { ui.onAllNodesWithContentDescription("Remove photo 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) } }
            catch (failure: Throwable) { screenshot("gallery-after-selection"); throw failure }
            screenshot("09-gallery-attached")
            ui.onNodeWithContentDescription("Remove photo 1").performScrollTo().performClick()
            ui.onNodeWithContentDescription("Remove photo 1").assertDoesNotExist()
        } finally { resolver.delete(uri, null, null) }
    }

    @Test fun directionsHandOffAndNoHandlerFallback() {
        org.junit.Assume.assumeTrue(InstrumentationRegistry.getArguments().getString("externalFlows") == "true")
        ui.onNodeWithText("Parking").performClick()
        ui.onAllNodes(hasText("Sample", substring = true) and hasClickAction()).onFirst().performScrollTo().performClick()
        ui.onNodeWithText("Directions").performScrollTo().performClick()
        ui.onNodeWithText("Open Maps").performClick()
        ui.waitUntil(15000) { val pkg = instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString()
            (pkg != null && pkg != instrumentation.targetContext.packageName) || runCatching {
                ui.onAllNodesWithText("Directions unavailable").fetchSemanticsNodes().isNotEmpty()
            }.getOrDefault(false) }
        screenshot("10-directions-hand-off")
        if (instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString() != instrumentation.targetContext.packageName)
            instrumentation.uiAutomation.executeShellCommand("input keyevent 4 4").close()
        val attempted = mutableListOf<Intent>()
        val blocked = object : ContextWrapper(instrumentation.targetContext) {
            override fun startActivity(intent: Intent) { attempted.add(intent); throw ActivityNotFoundException("Test: no handler") }
        }
        assertFalse(MapsDirectionsLauncher(blocked).open(51.454, -2.593, "Sample"))
        assertEquals(3, attempted.size)
    }
}
