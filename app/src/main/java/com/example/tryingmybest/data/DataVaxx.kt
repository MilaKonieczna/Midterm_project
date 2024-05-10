package com.example.tryingmybest.data
import java.util.Date

/**
 * Data class representing appointment information.
 * @property name The name of the vaccine. Defaults to an empty string.
 * @property nextDose The date of the next dose. Defaults to null.
 * @property lastDose The date of the last dose. Defaults to null.
 * @property desc Description of the vaccination. Defaults to an empty string.
 * @property expandable Boolean indicating whether additional information is expandable. Defaults to false.
 */
data class DataVaxx(
    var name: String = "",
    var nextDose: Date? = null,
    var lastDose: Date? = null,
    var desc: String = "",
    var expandable: Boolean = false
)