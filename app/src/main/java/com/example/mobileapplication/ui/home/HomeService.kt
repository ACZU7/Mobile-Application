package com.example.mobileapplication.ui.home

import androidx.annotation.StringRes
import com.example.mobileapplication.R

/**
 * The six service tiles on the Home dashboard (mockup screen 2), in grid order.
 *
 * @property labelRes The tile label.
 * @property keywordsRes Extra words the search also matches, for example
 * "pothole" for Report an issue. Kept in `strings.xml` so they can be translated.
 */
enum class HomeService(@StringRes val labelRes: Int, @StringRes val keywordsRes: Int) {
    FIND_PARKING(R.string.home_service_find_parking, R.string.home_service_find_parking_keywords),
    PERMIT_ZONES(R.string.home_service_permit_zones, R.string.home_service_permit_zones_keywords),
    REPORT_ISSUE(R.string.home_service_report_issue, R.string.home_service_report_issue_keywords),
    CLEAN_AIR_ZONE(R.string.home_service_clean_air_zone, R.string.home_service_clean_air_zone_keywords),
    REPORT_REPAIR(R.string.home_service_report_repair, R.string.home_service_report_repair_keywords),
    RENT_ACCOUNT(R.string.home_service_rent_account, R.string.home_service_rent_account_keywords)
}

/** Which greeting the Home header shows, from the time of day. */
enum class TimeOfDay {
    MORNING,
    AFTERNOON,
    EVENING;

    companion object {
        /** Morning from 5am, afternoon from 12pm, evening from 6pm (and through the night). */
        fun fromHour(hour: Int): TimeOfDay = when (hour) {
            in 5..11 -> MORNING
            in 12..17 -> AFTERNOON
            else -> EVENING
        }
    }
}

/** The search on the Home dashboard. Plain Kotlin, so it can be unit tested. */
object HomeSearch {

    /**
     * True when every word of [query] appears somewhere in [searchableText],
     * ignoring case. An empty query matches everything.
     */
    fun matches(query: String, searchableText: String): Boolean {
        val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val text = searchableText.lowercase()
        return words.all { it in text }
    }
}
