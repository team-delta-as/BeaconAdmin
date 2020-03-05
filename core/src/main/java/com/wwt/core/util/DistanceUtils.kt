package com.wwt.core.util

import java.util.*
import kotlin.math.ceil


private fun Locale.isMetric(): Boolean {
    return when (country.toUpperCase()) {
        "US", "LR", "MM" -> false
        else -> true
    }
}

fun getDistanceBasedOnLocale(distance: Double): String {
    return when {
        distance < 0 -> ""
        else -> when {
            (Locale.getDefault().isMetric()) -> "${distance.toInt()} m"
            else -> "${ceil(distance * 3.28084).toInt()} ft"
        }
    }
}