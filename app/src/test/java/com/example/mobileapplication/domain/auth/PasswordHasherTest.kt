package com.example.mobileapplication.domain.auth

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [PasswordHasher] and the stored [DemoAccounts] hashes. */
class PasswordHasherTest {

    @Test
    fun hash_matchesPublishedPbkdf2Sha256TestVector() {
        // PBKDF2-HMAC-SHA256, password "password", salt "salt", 1 iteration, 32 bytes.
        val expected = PasswordHasher.hexToBytes("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b")
        assertArrayEquals(expected, PasswordHasher.hash("password", "salt".toByteArray(), iterations = 1))
    }

    @Test
    fun demoAccounts_verifyWithTheirPasswords() {
        val resident = DemoAccounts.resident
        val admin = DemoAccounts.admin
        assertTrue(PasswordHasher.verify("Resident-Demo-2026", resident.salt, resident.hash))
        assertTrue(PasswordHasher.verify("Admin-Demo-2026", admin.salt, admin.hash))
    }

    @Test
    fun wrongPasswords_doNotVerify() {
        val resident = DemoAccounts.resident
        assertFalse(PasswordHasher.verify("resident-demo-2026", resident.salt, resident.hash))
        assertFalse(PasswordHasher.verify("", resident.salt, resident.hash))
        // Each account has its own salt, so one account's password doesn't open the other.
        assertFalse(PasswordHasher.verify("Admin-Demo-2026", resident.salt, resident.hash))
    }

    @Test
    fun samePasswordWithDifferentSalts_givesDifferentHashes() {
        val a = PasswordHasher.hash("same", ByteArray(16) { 1 }, iterations = 10)
        val b = PasswordHasher.hash("same", ByteArray(16) { 2 }, iterations = 10)
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun demoAccounts_storeOnlySaltsAndFullLengthHashes() {
        DemoAccounts.all.forEach {
            assertEquals(16, it.salt.size)
            assertEquals(PasswordHasher.KEY_LENGTH_BYTES, it.hash.size)
        }
    }

    @Test
    fun constantTimeEquals_comparesContentAndLength() {
        assertTrue(PasswordHasher.constantTimeEquals(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 3)))
        assertFalse(PasswordHasher.constantTimeEquals(byteArrayOf(1, 2, 3), byteArrayOf(1, 2, 4)))
        assertFalse(PasswordHasher.constantTimeEquals(byteArrayOf(1, 2, 3), byteArrayOf(1, 2)))
        assertFalse(PasswordHasher.constantTimeEquals(byteArrayOf(1, 2), byteArrayOf(1, 2, 0)))
        assertTrue(PasswordHasher.constantTimeEquals(ByteArray(0), ByteArray(0)))
    }

    @Test
    fun hexToBytes_decodes() {
        assertArrayEquals(byteArrayOf(0, 0x0f, -1), PasswordHasher.hexToBytes("000fff"))
    }
}
