package com.example.mobileapplication.ui.housing

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats an amount in pence as pounds, for example 52000 → "£520.00".
 *
 * UK formatting is used whatever the phone's language, because rent in Bristol
 * is always in pounds sterling.
 */
fun formatPence(pence: Long): String =
    NumberFormat.getCurrencyInstance(Locale.UK).format(BigDecimal.valueOf(pence, 2))

/** UK long date style, for example "1 December 2026", as in the rent account mockup. */
private val longDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)

/** Formats a date with the month written in full, for example "1 December 2026". */
fun formatLongDate(date: LocalDate): String = date.format(longDateFormatter)
