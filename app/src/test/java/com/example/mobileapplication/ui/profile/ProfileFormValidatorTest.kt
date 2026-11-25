package com.example.mobileapplication.ui.profile

import com.example.mobileapplication.R
import com.example.mobileapplication.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [ProfileFormValidator] and the [UserProfile] helpers. */
class ProfileFormValidatorTest {

    @Test
    fun emptyForm_isValid() {
        assertTrue(ProfileFormValidator.validate(ProfileFormState()).isValid)
    }

    @Test
    fun validEmails_areAccepted() {
        listOf("sam@example.com", "sam.wilson+caz@bristol.gov.uk", " a_b@c.io ").forEach {
            assertTrue(it, ProfileFormValidator.isValidEmail(it))
        }
    }

    @Test
    fun invalidEmails_areRejected() {
        listOf("sam", "sam@", "@example.com", "sam@example", "sam wilson@example.com", "sam@example.c").forEach {
            assertFalse(it, ProfileFormValidator.isValidEmail(it))
        }
    }

    @Test
    fun validPostcodes_areNormalised() {
        assertEquals("BS1 5TR", ProfileFormValidator.normalisePostcode("bs15tr"))
        assertEquals("BS16 1QY", ProfileFormValidator.normalisePostcode(" BS16 1QY "))
        assertEquals("SW1A 1AA", ProfileFormValidator.normalisePostcode("sw1a1aa"))
        assertEquals("M1 1AE", ProfileFormValidator.normalisePostcode("M1 1AE"))
        assertEquals("EC1A 1BB", ProfileFormValidator.normalisePostcode("EC1A 1BB"))
    }

    @Test
    fun invalidPostcodes_areRejected() {
        listOf("BS99", "12345", "BS1 5T", "QS1 5TR", "BS1 5CI", "BS1-5TR", "BS1 5TRX").forEach {
            assertNull(it, ProfileFormValidator.normalisePostcode(it))
        }
    }

    @Test
    fun registration_usesTheCazValidator() {
        val checked = ProfileFormValidator.validate(ProfileFormState(vehicleRegistration = "ABCDEF"))
        assertEquals(R.string.caz_error_no_digits, checked.vehicleError)
    }

    @Test
    fun nameTooLong_isRejected() {
        val checked = ProfileFormValidator.validate(ProfileFormState(name = "a".repeat(51)))
        assertEquals(R.string.profile_error_name_too_long, checked.nameError)
    }

    @Test
    fun toProfile_trimsAndDropsBlankFields() {
        val profile = ProfileFormValidator.toProfile(
            ProfileFormState(name = "  ", email = " sam@example.com ", postcode = "bs1 5tr", vehicleRegistration = "")
        )
        assertEquals(UserProfile(email = "sam@example.com", postcode = "BS1 5TR"), profile)
    }

    @Test
    fun initials_useFirstAndLastWords() {
        assertEquals("SW", UserProfile(name = "Sam Wilson").initials)
        assertEquals("SW", UserProfile(name = "sam j wilson").initials)
        assertEquals("S", UserProfile(name = "Sam").initials)
        assertNull(UserProfile().initials)
    }

    @Test
    fun firstName_isTheFirstWord() {
        assertEquals("Sam", UserProfile(name = " Sam  Wilson").firstName)
        assertNull(UserProfile().firstName)
    }
}
