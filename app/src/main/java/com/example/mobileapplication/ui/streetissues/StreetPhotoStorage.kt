package com.example.mobileapplication.ui.streetissues

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Street report photos live in this app's private storage, so they need no storage permission,
 * are not visible to other apps and are removed when the app is uninstalled. Nothing is uploaded.
 */
object StreetPhotoStorage {
    private const val FOLDER = "street_photos"

    /** A new, empty file for the camera to write a capture into. */
    fun newPhotoFile(context: Context): File {
        val folder = File(context.filesDir, FOLDER).apply { mkdirs() }
        return File(folder, "street_${System.currentTimeMillis()}.jpg")
    }

    /** Deletes a photo the user removed from the form. Only files inside this app's photo folder are touched. */
    fun delete(context: Context, uri: String) {
        val path = Uri.parse(uri).path ?: return
        val file = File(path)
        val folder = File(context.filesDir, FOLDER)
        if (file.parentFile?.canonicalPath == folder.canonicalPath) file.delete()
    }

    /**
     * Copies a photo chosen in the system photo picker into app storage. The picker only grants
     * temporary access, so the copy keeps the photo available for the saved report.
     * Returns the copy's URI, or null if the photo could not be read.
     */
    suspend fun copyFromPicker(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        val target = newPhotoFile(context)
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            Uri.fromFile(target).toString()
        } catch (e: IOException) {
            target.delete()
            null
        } catch (e: SecurityException) {
            target.delete()
            null
        }
    }
}
