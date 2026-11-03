package com.example.mobileapplication.ui.cleanairzone

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/**
 * Opens a web address in another app, such as the browser.
 *
 * It is an interface so [CleanAirZoneViewModel] can be unit tested with a fake
 * that pretends no browser is installed.
 */
fun interface LinkOpener {

    /** Tries to open [url]. Returns false if no app on the phone can open it. */
    fun open(url: String): Boolean
}

/**
 * The real [LinkOpener]: asks Android to show the address in whichever app
 * handles web links.
 *
 * @param context The screen's context, used to start the other app.
 */
class BrowserLinkOpener(private val context: Context) : LinkOpener {

    override fun open(url: String): Boolean = try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        true
    } catch (e: ActivityNotFoundException) {
        // No browser (or other app) can open web links on this phone.
        false
    }
}
