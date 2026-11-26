package com.example.mobileapplication.domain.auth

import com.example.mobileapplication.data.model.Role

/**
 * One fictional demo account. Only the salt and the PBKDF2 hash of the password
 * are kept; the password itself is not in the code.
 *
 * @property email The sign-in email, in lower case.
 * @property role What the account may do.
 * @property saltHex The random salt, as hex.
 * @property hashHex PBKDF2-HMAC-SHA256 of the password with [saltHex], as hex.
 */
data class DemoAccount(
    val email: String,
    val role: Role,
    val saltHex: String,
    val hashHex: String
) {
    val salt: ByteArray get() = PasswordHasher.hexToBytes(saltHex)
    val hash: ByteArray get() = PasswordHasher.hexToBytes(hashHex)
}

/**
 * The two prototype sample accounts. They are not Council accounts and give no
 * access to real data. Their passwords are shown on the Login screen's
 * "Demo accounts" note for testers.
 */
object DemoAccounts {

    val resident = DemoAccount(
        email = "resident@example.com",
        role = Role.RESIDENT,
        saltHex = "f56053474b74498a8c6aefefe7e14634",
        hashHex = "649f8f0e89fc7e91d03b1f840fbb6098e6e0351ba8c93df3519afffa8aa0f5c3"
    )

    val admin = DemoAccount(
        email = "admin@example.com",
        role = Role.ADMIN,
        saltHex = "9241f23d506787041018847ded9c6484",
        hashHex = "ed9df47479bcb49c38d7946db2bbd107ec1874efd7d23df56945e54edd87a923"
    )

    val all: List<DemoAccount> = listOf(resident, admin)
}
