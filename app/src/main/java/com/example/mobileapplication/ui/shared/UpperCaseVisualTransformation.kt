package com.example.mobileapplication.ui.shared

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Shows a text field's text in capitals without changing the text itself.
 *
 * Use it instead of calling `uppercase()` in `onValueChange`: rewriting the
 * text while the keyboard is still composing a word can make the keyboard drop
 * letters (for example "IB15PTL" becoming "IB15PL"). Convert to capitals when
 * the value is used, for example when checking or saving.
 *
 * Each character is converted on its own, so the shown text always has the same
 * length as the typed text and the cursor stays in the right place.
 */
object UpperCaseVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val shown = buildString(text.length) { text.text.forEach { append(it.uppercaseChar()) } }
        return TransformedText(AnnotatedString(shown), OffsetMapping.Identity)
    }
}
