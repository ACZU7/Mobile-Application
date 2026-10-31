package com.example.mobileapplication.data.model

/**
 * The result of checking one vehicle against the prototype Clean Air Zone rules.
 *
 * This is a demonstration result only. It is worked out on the phone from sample
 * data and is **not** an official charge decision from Bristol City Council.
 *
 * The model is plain Kotlin with no string resources, so the rules can be unit
 * tested without Android. The Clean Air Zone screens turn each enum value into a
 * readable label from `strings.xml`.
 *
 * @property registration The registration the user entered, for example "IB15 PTL".
 * @property make The manufacturer, or null if the registration is not in the sample data.
 * @property model The model name, or null if the registration is not in the sample data.
 * @property vehicleType The kind of vehicle, or null if the registration is not in the sample data.
 * @property fuelType The vehicle's fuel, or null if the registration is not in the sample data.
 * @property euroStandard The vehicle's emissions standard, or null if it is not known
 * (electric vehicles do not have one).
 * @property outcome Whether a sample charge applies.
 * @property dailyChargePence The sample daily charge in pence. It is only set when
 * [outcome] is [CazOutcome.CHARGE_APPLIES]; otherwise it is null.
 */
data class CazCheckResult(
    val registration: String,
    val make: String?,
    val model: String?,
    val vehicleType: VehicleType?,
    val fuelType: FuelType?,
    val euroStandard: EuroStandard?,
    val outcome: CazOutcome,
    val dailyChargePence: Int? = null
)

/** The possible answers from the prototype Clean Air Zone check. */
enum class CazOutcome {
    /** The vehicle meets the sample emissions standard, so there is no charge. */
    NO_CHARGE,

    /** The vehicle does not meet the sample standard, so a sample daily charge applies. */
    CHARGE_APPLIES,

    /** There is not enough sample information to give an answer. */
    UNKNOWN
}

/** The kinds of vehicle the prototype checker knows about. */
enum class VehicleType {
    CAR,
    TAXI,
    VAN,
    BUS,
    HGV
}

/** The fuel types the prototype checker knows about. */
enum class FuelType {
    PETROL,
    DIESEL,
    ELECTRIC
}

/**
 * European emissions standards, from oldest (Euro 1) to newest (Euro 6).
 *
 * @property level The standard's number, so standards can be compared, for example
 * "Euro 4 or newer" is `level >= 4`.
 */
enum class EuroStandard(val level: Int) {
    EURO_1(1),
    EURO_2(2),
    EURO_3(3),
    EURO_4(4),
    EURO_5(5),
    EURO_6(6)
}
