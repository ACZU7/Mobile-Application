package com.example.mobileapplication.ui.shared

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for [UpperCaseVisualTransformation]. */
class UpperCaseVisualTransformationTest {

    private fun shown(typed: String) = UpperCaseVisualTransformation.filter(AnnotatedString(typed))

    @Test
    fun showsCapitals() {
        assertEquals("IB15 PTL", shown("ib15 ptl").text.text)
    }

    @Test
    fun keepsTheSameLengthAndCursorPositions() {
        // "ß" becomes "SS" with String.uppercase(); converting one character at a time keeps the length.
        val result = shown("aß1")
        assertEquals(3, result.text.length)
        assertEquals(2, result.offsetMapping.originalToTransformed(2))
    }
}
