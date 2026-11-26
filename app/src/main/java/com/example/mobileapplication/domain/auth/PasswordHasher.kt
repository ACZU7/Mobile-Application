package com.example.mobileapplication.domain.auth

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashes and checks passwords with PBKDF2-HMAC-SHA256.
 *
 * Only the salt and the hash are ever stored; the password itself is not.
 * Plain Kotlin and the standard Java crypto API, so it runs in JVM unit tests too.
 */
object PasswordHasher {

    /** How many PBKDF2 rounds are used. Higher is slower for an attacker (and for sign-in). */
    const val ITERATIONS = 120_000

    /** Length of the derived key in bytes (256 bits). */
    const val KEY_LENGTH_BYTES = 32

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    /** Derives the PBKDF2 hash of [password] with [salt]. */
    fun hash(password: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val chars = password.toCharArray()
        val spec = PBEKeySpec(chars, salt, iterations, KEY_LENGTH_BYTES * 8)
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            // Don't leave the password lying around in memory longer than needed.
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    /** True when [password] hashes to [expectedHash] with [salt]. The comparison takes constant time. */
    fun verify(password: String, salt: ByteArray, expectedHash: ByteArray, iterations: Int = ITERATIONS): Boolean =
        constantTimeEquals(hash(password, salt, iterations), expectedHash)

    /**
     * Compares two byte arrays without stopping at the first difference, so the
     * time taken does not reveal how much of a hash was right.
     */
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        var difference = a.size xor b.size
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = if (i < a.size) a[i].toInt() else 0
            val y = if (i < b.size) b[i].toInt() else 0
            difference = difference or (x xor y)
        }
        return difference == 0
    }

    /** Turns a hex string such as "0aff" into bytes. */
    fun hexToBytes(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have an even length" }
        return ByteArray(hex.length / 2) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }
}
