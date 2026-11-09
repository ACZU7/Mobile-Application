package com.example.mobileapplication.ui.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Map marker images drawn on the device, in the app's theme colours (passed in as ARGB ints).
 * They follow the approved mockups: teal "P" pins, the selected pin coral with a spaces label underneath.
 */
object MapMarkers {

    /** A teardrop pin with a letter, and optionally a rounded label below it (for the selected car park). */
    fun pin(context: Context, fill: Int, outline: Int, letter: String?, letterColor: Int, label: String? = null, labelFill: Int = 0, labelText: Int = 0): BitmapDrawable {
        val d = context.resources.displayMetrics.density
        val pinW = 30 * d
        val pinH = 40 * d
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 12 * d; color = labelText; typeface = Typeface.DEFAULT_BOLD }
        val labelW = label?.let { labelPaint.measureText(it) + 16 * d } ?: 0f
        val labelH = if (label != null) 24 * d else 0f
        val width = maxOf(pinW + 4 * d, labelW)
        val height = pinH + 4 * d + if (label != null) labelH + 4 * d else 0f
        val bitmap = Bitmap.createBitmap(width.toInt().coerceAtLeast(1), height.toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = width / 2
        val top = 2 * d
        val path = Path().apply {
            moveTo(cx, top + pinH)
            cubicTo(cx - pinW * 0.6f, top + pinH * 0.42f, cx - pinW / 2, top, cx, top)
            cubicTo(cx + pinW / 2, top, cx + pinW * 0.6f, top + pinH * 0.42f, cx, top + pinH)
            close()
        }
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = outline; style = Paint.Style.STROKE; strokeWidth = 3 * d })
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
        if (letter != null) {
            val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = letterColor; textSize = 15 * d; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
            }
            canvas.drawText(letter, cx, top + pinH * 0.45f, letterPaint)
        }
        if (label != null) {
            val labelTop = top + pinH + 4 * d
            canvas.drawRoundRect(RectF(cx - labelW / 2, labelTop, cx + labelW / 2, labelTop + labelH), 4 * d, 4 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = labelFill })
            canvas.drawText(label, cx - labelPaint.measureText(label) / 2, labelTop + labelH / 2 - (labelPaint.descent() + labelPaint.ascent()) / 2, labelPaint)
        }
        return BitmapDrawable(context.resources, bitmap)
    }

    /** Where the pin's tip sits inside [pin]'s image, for Marker.setAnchor. */
    fun pinAnchorY(drawable: BitmapDrawable, context: Context): Float {
        val d = context.resources.displayMetrics.density
        return ((2 + 40) * d) / drawable.bitmap.height
    }

    /** The "you are here" dot: a filled circle with a white ring and a soft halo. */
    fun locationDot(context: Context, color: Int, ring: Int): BitmapDrawable {
        val d = context.resources.displayMetrics.density
        val size = (36 * d).toInt()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val c = size / 2f
        canvas.drawCircle(c, c, 16 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; alpha = 46 })
        canvas.drawCircle(c, c, 8 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = ring })
        canvas.drawCircle(c, c, 6 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
        return BitmapDrawable(context.resources, bitmap)
    }

    /** A rounded text badge, such as the "Zone CM" label on the permit map. */
    fun badge(context: Context, text: String, fill: Int, textColor: Int): BitmapDrawable {
        val d = context.resources.displayMetrics.density
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 15 * d; color = textColor; typeface = Typeface.DEFAULT_BOLD }
        val w = paint.measureText(text) + 24 * d
        val h = 32 * d
        val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRoundRect(RectF(0f, 0f, w, h), 6 * d, 6 * d, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
        canvas.drawText(text, 12 * d, h / 2 - (paint.descent() + paint.ascent()) / 2, paint)
        return BitmapDrawable(context.resources, bitmap)
    }
}

/** Adds a non-interactive marker (no info bubble). */
fun MapView.addMarker(point: GeoPoint, icon: BitmapDrawable, anchorY: Float = Marker.ANCHOR_CENTER, onClick: (() -> Unit)? = null): Marker =
    Marker(this).apply {
        position = point
        this.icon = icon
        setAnchor(Marker.ANCHOR_CENTER, anchorY)
        setInfoWindow(null)
        setOnMarkerClickListener { _, _ -> onClick?.invoke(); onClick != null }
    }.also { overlays.add(it) }

/** Moves the camera to show every point, with a margin; one point is shown at street level. */
fun MapView.frame(points: List<GeoPoint>, singlePointZoom: Double = 15.0, topOverlayFraction: Double = 0.0) {
    if (points.isEmpty()) return
    val apply = {
        val box = MapFraming.boundsFor(points)?.let { MapFraming.withTopSpace(it, topOverlayFraction) }
        if (box == null) {
            controller.setZoom(singlePointZoom)
            controller.setCenter(points.first())
        } else {
            zoomToBoundingBox(BoundingBox(box.north, box.east, box.south, box.west), false, (48 * resources.displayMetrics.density).toInt())
        }
    }
    if (width > 0 && height > 0) apply() else addOnFirstLayoutListener { _, _, _, _, _ -> apply() }
}
