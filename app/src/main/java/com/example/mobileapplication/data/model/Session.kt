package com.example.mobileapplication.data.model

/** What a signed-in user may do. Local demonstration roles only; not Council accounts. */
enum class Role {
    /** "Continue as guest": every resident feature, no account. */
    GUEST,

    /** A demo resident account: every resident feature. */
    RESIDENT,

    /** A demo admin account: resident features plus the staff (Admin) dashboard. */
    ADMIN
}

/**
 * Who is using the app right now.
 *
 * @property email The demo account's email, or null for a guest.
 * @property role What they may do.
 */
data class Session(val email: String?, val role: Role) {
    companion object {
        /** The session saved when the user taps "Continue as guest". */
        val Guest = Session(email = null, role = Role.GUEST)
    }
}
