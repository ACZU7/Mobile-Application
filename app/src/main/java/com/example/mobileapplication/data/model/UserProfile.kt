package com.example.mobileapplication.data.model

/**
 * The local demonstration profile.
 *
 * It is stored only on this phone and is not a Bristol City Council account.
 * Every field is optional, so a resident can use the app without filling any of it in.
 *
 * @property name The name used in the Home greeting, for example "Sam Wilson".
 * @property email A contact email, already checked for a valid format.
 * @property postcode A UK postcode in standard form, for example "BS1 5TR".
 * @property vehicleRegistration A saved number plate in normalised form (no spaces,
 * capitals), for example "AB12CDE", used by the Clean Air Zone checker.
 */
data class UserProfile(
    val name: String? = null,
    val email: String? = null,
    val postcode: String? = null,
    val vehicleRegistration: String? = null
) {
    /** True when nothing has been saved yet. */
    val isEmpty: Boolean
        get() = name == null && email == null && postcode == null && vehicleRegistration == null

    /** The first word of [name], for a short greeting such as "Good morning, Sam". */
    val firstName: String?
        get() = name?.trim()?.split(WHITESPACE)?.firstOrNull()?.takeIf { it.isNotEmpty() }

    /**
     * Up to two initials for the avatar: the first letters of the first and last
     * words of [name], for example "SW" for "Sam Wilson". Null when there is no name.
     */
    val initials: String?
        get() {
            val words = name?.trim()?.split(WHITESPACE)?.filter { it.isNotEmpty() }.orEmpty()
            if (words.isEmpty()) return null
            val letters = if (words.size == 1) words.first().take(1) else words.first().take(1) + words.last().take(1)
            return letters.uppercase()
        }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
