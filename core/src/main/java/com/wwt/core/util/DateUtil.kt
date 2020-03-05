package com.wwt.core.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    fun getFormattedTimeStamp(date: Date): String {
        return SimpleDateFormat(DATE_FORMAT).format(date.time)
    }

    fun getGeofenceExpirationDuration(): Long {
        return GEOFENCE_EXPIRY_DAYS
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm.ss Z"
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000
        private const val GEOFENCE_EXPIRY_DAYS: Long = 180L * MILLIS_PER_DAY
    }
}